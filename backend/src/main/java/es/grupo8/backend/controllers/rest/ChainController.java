/**
 * Controlador REST para gestionar cadenas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dto.ChainDTO;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.ChainService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/chains")
@AllArgsConstructor
public class ChainController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final ChainService chainService;
    private final AuthService authService;
    private final UserService userService;

    private void checkAdmin(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<List<ChainDTO>> getChains(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        List<ChainDTO> chains = chainService.findAll();
        auditLog.info("ACTION=LIST_CHAINS userId={} timestamp={} total={}",
                authService.extractUserIdFromToken(authHeader), Instant.now(), chains.size());
        return ResponseEntity.ok(chains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChainDTO> getChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(chainService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ChainDTO> createChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ChainDTO req) {

        checkAdmin(authHeader);
        ChainDTO created = chainService.create(req);
        auditLog.info("ACTION=CREATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}'",
                authService.extractUserIdFromToken(authHeader), Instant.now(), created.getId(), created.getName(), created.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChainDTO> updateChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) ChainDTO req) {

        checkAdmin(authHeader);
        ChainDTO updated = chainService.update(id, req);
        auditLog.info("ACTION=UPDATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}'",
                authService.extractUserIdFromToken(authHeader), Instant.now(), updated.getId(), updated.getName(), updated.getCode());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        chainService.delete(id);
        auditLog.info("ACTION=DELETE_CHAIN userId={} timestamp={} chainId={}",
                authService.extractUserIdFromToken(authHeader), Instant.now(), id);
        return ResponseEntity.ok(Map.of("message", "Chain deleted successfully"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }
}