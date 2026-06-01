/**
 * Controlador REST para gestionar cadenas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import es.grupo8.backend.dto.ChainDTO;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.ChainService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/chains")
@AllArgsConstructor
public class ChainController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final ChainService chainService;
    private final AdminGuard   adminGuard;

    @GetMapping
    public ResponseEntity<?> getChains(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        List<ChainDTO> chains = chainService.findAll();
        auditLog.info("ACTION=LIST_CHAINS userId={} timestamp={} total={}",
                adminGuard.extractUserId(authHeader), Instant.now(), chains.size());
        return ResponseEntity.ok(chains);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            return ResponseEntity.ok(chainService.findById(id));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ChainDTO req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            ChainDTO created = chainService.create(req);
            auditLog.info("ACTION=CREATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}'",
                    adminGuard.extractUserId(authHeader), Instant.now(), created.getId(), created.getName(), created.getCode());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) ChainDTO req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            ChainDTO updated = chainService.update(id, req);
            auditLog.info("ACTION=UPDATE_CHAIN userId={} timestamp={} chainId={} name='{}' code='{}'",
                    adminGuard.extractUserId(authHeader), Instant.now(), updated.getId(), updated.getName(), updated.getCode());
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            chainService.delete(id);
            auditLog.info("ACTION=DELETE_CHAIN userId={} timestamp={} chainId={}",
                    adminGuard.extractUserId(authHeader), Instant.now(), id);
            return ResponseEntity.ok(Map.of("message", "Chain deleted successfully"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }
}