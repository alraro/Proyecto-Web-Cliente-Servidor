/**
 * Controlador REST para gestionar tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.StoreService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@AllArgsConstructor
public class StoreController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final StoreService storeService;
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

    private void checkAdminOrCoordinator(String authHeader) {
        Integer userId = authService.extractUserIdFromToken(authHeader);
        if (userId == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!userService.isAdmin(userId) && !userService.isCoordinator(userId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "No tienes permiso");
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer localityId,
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        checkAdminOrCoordinator(authHeader);

        Map<String, Object> result = storeService.findAll(chainId, localityId, zoneId, page, size);
        auditLog.info("ACTION=LIST_STORES userId={} timestamp={} total={}",
                authService.extractUserIdFromToken(authHeader), Instant.now(), result.get("totalElements"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(storeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<StoreDTO> createStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) StoreDTO req) {

        checkAdmin(authHeader);
        StoreDTO created = storeService.create(req);
        auditLog.info("ACTION=CREATE_STORE userId={} timestamp={} storeId={} name='{}'",
                authService.extractUserIdFromToken(authHeader), Instant.now(), created.getId(), created.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDTO> updateStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) StoreDTO req) {

        checkAdmin(authHeader);
        StoreDTO updated = storeService.update(id, req);
        auditLog.info("ACTION=UPDATE_STORE userId={} timestamp={} storeId={} name='{}'",
                authService.extractUserIdFromToken(authHeader), Instant.now(), updated.getId(), updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        checkAdmin(authHeader);
        storeService.delete(id);
        auditLog.info("ACTION=DELETE_STORE userId={} timestamp={} storeId={}",
                authService.extractUserIdFromToken(authHeader), Instant.now(), id);
        return ResponseEntity.ok(Map.of("message", "Store deleted successfully"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException e) {
        return ResponseEntity.status(e.getStatus())
                .body(Map.of("message", e.getMessage()));
    }
}