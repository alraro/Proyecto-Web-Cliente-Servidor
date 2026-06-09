package es.grupo8.backend.controllers.rest;

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

import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.CampaignStoreService;
import lombok.AllArgsConstructor;

/**
 * REST controller for RF-12: administration of campaign-store assignments.
 *
 * Endpoints:
 * GET    /api/campaigns/{campaignId}/stores
 * POST   /api/campaigns/{campaignId}/stores
 * DELETE /api/campaigns/{campaignId}/stores/{storeId}
 * PUT    /api/campaigns/{campaignId}/stores
 */
@RestController
@AllArgsConstructor
public class CampaignStoreRestController {

    private final AdminGuard adminGuard;
    private final CampaignStoreService campaignStoreService;

    // ── Auth helper ───────────────────────────────────────────────────────────

    /**
     * Validates the Authorization header and asserts admin role.
     *
     * @param auth raw Authorization header value
     * @throws AuthException 401 if token is missing/invalid, 403 if not admin
     */
    private void checkAdmin(String auth) {
        if (adminGuard.extractUserId(auth) == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!adminGuard.isAdmin(auth)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Access restricted to administrators");
        }
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Returns all stores assigned to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId campaign identifier
     * @return list of campaign-store DTOs
     */
    @GetMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> getCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(campaignStoreService.getCampaignStores(
                adminGuard.extractUserId(authHeader), campaignId));
    }

    /**
     * Assigns a single store to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId campaign identifier
     * @param request    body containing storeId
     * @return assignment DTO with 201 status
     */
    @PostMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> assignStoreToCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        checkAdmin(authHeader);
        Integer storeId = valueAsInteger(request == null ? null : request.get("storeId"));
        if (storeId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeId is required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignStoreService.assignStoreToCampaign(
                        adminGuard.extractUserId(authHeader), campaignId, storeId));
    }

    /**
     * Removes a store from a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId campaign identifier
     * @param storeId    store identifier
     * @return success message
     */
    @DeleteMapping("/api/campaigns/{campaignId}/stores/{storeId}")
    public ResponseEntity<?> removeStoreFromCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer storeId) {

        checkAdmin(authHeader);
        campaignStoreService.removeStoreFromCampaign(
                adminGuard.extractUserId(authHeader), campaignId, storeId);
        return ResponseEntity.ok(Map.of("message", "Tienda desasignada correctamente"));
    }

    /**
     * Replaces the full set of stores for a campaign in a single atomic operation.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId campaign identifier
     * @param request    body containing storeIds list
     * @return updated list of campaign-store DTOs
     */
    @PutMapping("/api/campaigns/{campaignId}/stores")
    public ResponseEntity<?> replaceCampaignStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        checkAdmin(authHeader);
        List<Integer> storeIds = valueAsIntegerList(request == null ? null : request.get("storeIds"));
        if (storeIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeIds list is required"));
        }
        return ResponseEntity.ok(campaignStoreService.replaceCampaignStores(
                adminGuard.extractUserId(authHeader), campaignId, storeIds));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e entity not found */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /** @param e conflict / already assigned */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

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
