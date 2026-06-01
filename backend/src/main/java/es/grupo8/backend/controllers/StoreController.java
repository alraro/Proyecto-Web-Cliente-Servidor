/**
 * Controlador REST para gestionar tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.security.CoordinatorGuard;
import es.grupo8.backend.services.StoreService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/stores")
@AllArgsConstructor
public class StoreController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final StoreService     storeService;
    private final AdminGuard       adminGuard;
    private final CoordinatorGuard coordinatorGuard;

    @GetMapping
    public ResponseEntity<?> getStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) Integer chainId,
            @RequestParam(required = false) Integer localityId,
            @RequestParam(required = false) Integer zoneId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        if (!adminGuard.isAdmin(authHeader) && !coordinatorGuard.isCoordinator(authHeader))
            return forbidden();

        Map<String, Object> result = storeService.findAll(chainId, localityId, zoneId, page, size);
        auditLog.info("ACTION=LIST_STORES userId={} timestamp={} total={}",
                adminGuard.extractUserId(authHeader), Instant.now(), result.get("totalElements"));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            return ResponseEntity.ok(storeService.findById(id));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) StoreDTO req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            StoreDTO created = storeService.create(req);
            auditLog.info("ACTION=CREATE_STORE userId={} timestamp={} storeId={} name='{}'",
                    adminGuard.extractUserId(authHeader), Instant.now(), created.getId(), created.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) StoreDTO req) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            StoreDTO updated = storeService.update(id, req);
            auditLog.info("ACTION=UPDATE_STORE userId={} timestamp={} storeId={} name='{}'",
                    adminGuard.extractUserId(authHeader), Instant.now(), updated.getId(), updated.getName());
            return ResponseEntity.ok(updated);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStore(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        try {
            storeService.delete(id);
            auditLog.info("ACTION=DELETE_STORE userId={} timestamp={} storeId={}",
                    adminGuard.extractUserId(authHeader), Instant.now(), id);
            return ResponseEntity.ok(Map.of("message", "Store deleted successfully"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason()));
        }
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }
}