package es.grupo8.backend.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.CampaignStoreService;
import lombok.AllArgsConstructor;

/**
 * RF-12: administration of campaign-store assignments.
 *
 * Endpoints:
 * GET    /api/campaigns/{campaignId}/stores
 * POST   /api/campaigns/{campaignId}/stores
 * DELETE /api/campaigns/{campaignId}/stores/{storeId}
 * PUT    /api/campaigns/{campaignId}/stores
 */
@RestController
@AllArgsConstructor
public class CampaignStoreController {

    private final AdminGuard adminGuard;
    private final CampaignStoreService campaignStoreService;

    @GetMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> getCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        return ResponseEntity.ok(campaignStoreService.getCampaignStores(
                adminGuard.extractUserId(authHeader), campaignId));
    }

    @PostMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> assignStoreToCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        Integer storeId = valueAsInteger(request == null ? null : request.get("storeId"));
        if (storeId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeId is required"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignStoreService.assignStoreToCampaign(
                        adminGuard.extractUserId(authHeader), campaignId, storeId));
    }

    @DeleteMapping("/api/campaigns/{campaignId}/stores/{storeId}")
    public ResponseEntity<?> removeStoreFromCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer storeId) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();
        campaignStoreService.removeStoreFromCampaign(
                adminGuard.extractUserId(authHeader), campaignId, storeId);
        return ResponseEntity.ok(Map.of("message", "Tienda desasignada correctamente"));
    }

    @PutMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> replaceCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!adminGuard.isAdmin(authHeader)) return forbidden();

        List<Integer> storeIds = valueAsIntegerList(request == null ? null : request.get("storeIds"));
        if (storeIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeIds list is required"));
        }

        return ResponseEntity.ok(campaignStoreService.replaceCampaignStores(
                adminGuard.extractUserId(authHeader), campaignId, storeIds));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access restricted to administrators"));
    }

    private static Integer valueAsInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer i) return i;
        if (value instanceof Number n) return n.intValue();
        try { return Integer.valueOf(String.valueOf(value)); } catch (NumberFormatException ex) { return null; }
    }

    private static List<Integer> valueAsIntegerList(Object value) {
        if (!(value instanceof List<?> rawList)) return null;
        List<Integer> result = new ArrayList<>();
        for (Object item : rawList) {
            Integer parsed = valueAsInteger(item);
            if (parsed == null) return null;
            result.add(parsed);
        }
        return result;
    }
}
