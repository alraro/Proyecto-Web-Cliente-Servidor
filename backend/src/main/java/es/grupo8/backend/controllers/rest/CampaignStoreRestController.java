/**
 * Controlador REST para gestionar las tiendas de una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 70%
 * - Alejandra Ortiz: 5%
 * - Alfonso Ramos Rojas: 5%
 * - IA Generativa: 20%
 */
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

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignStoreService;
import es.grupo8.backend.services.UserService;
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

    private final AuthService authService;
    private final UserService userService;
    private final CampaignStoreService campaignStoreService;

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

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(campaignStoreService.getCampaignStores(
                authService.extractUserIdFromToken(authHeader), campaignId));
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

        if (!userService.isAdminFromToken(authHeader)) return forbidden();

        Integer storeId = valueAsInteger(request == null ? null : request.get("storeId"));
        if (storeId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeId is required"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignStoreService.assignStoreToCampaign(
                        authService.extractUserIdFromToken(authHeader), campaignId, storeId));
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

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        campaignStoreService.removeStoreFromCampaign(
                authService.extractUserIdFromToken(authHeader), campaignId, storeId);
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

        if (!userService.isAdminFromToken(authHeader)) return forbidden();

        List<Integer> storeIds = valueAsIntegerList(request == null ? null : request.get("storeIds"));
        if (storeIds == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "storeIds list is required"));
        }
        return ResponseEntity.ok(campaignStoreService.replaceCampaignStores(
                authService.extractUserIdFromToken(authHeader), campaignId, storeIds));
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

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access restricted to administrators"));
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
