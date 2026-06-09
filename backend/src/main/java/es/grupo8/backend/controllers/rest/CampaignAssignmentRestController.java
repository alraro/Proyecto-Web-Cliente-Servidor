package es.grupo8.backend.controllers.rest;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.security.AdminGuard;
import es.grupo8.backend.services.CampaignAssignmentService;
import lombok.AllArgsConstructor;

/**
 * REST controller for assigning coordinators and captains to campaigns.
 * All operations are restricted to administrators.
 */
@RestController
@RequestMapping("/api/campaigns")
@AllArgsConstructor
public class CampaignAssignmentRestController {

    private final AdminGuard adminGuard;
    private final CampaignAssignmentService campaignAssignmentService;

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
     * Returns the list of campaigns available for assignment (admin view).
     *
     * @param authHeader JWT Authorization header
     * @return list of {@link es.grupo8.backend.dto.CampaignDTO}
     */
    @GetMapping("/admin-list")
    public ResponseEntity<?> getCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(campaignAssignmentService.getCampaigns(adminGuard.extractUserId(authHeader)));
    }

    /**
     * Returns the current coordinator and captain assignments for a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @return {@link es.grupo8.backend.dto.CampaignAssignmentsDTO} with coordinator and captain lists
     */
    @GetMapping("/{campaignId}/assignments")
    public ResponseEntity<?> getCampaignAssignments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(campaignAssignmentService.getCampaignAssignments(
                adminGuard.extractUserId(authHeader), campaignId));
    }

    /**
     * Returns users available to be assigned to a campaign filtered by role.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @param role       optional role filter (COORDINATOR or CAPTAIN)
     * @return list of {@link es.grupo8.backend.dto.UserDTO} for available users
     */
    @GetMapping("/{campaignId}/available-users")
    public ResponseEntity<?> getAvailableUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestParam(value = "role", required = false) String role) {

        checkAdmin(authHeader);
        return ResponseEntity.ok(campaignAssignmentService.getAvailableUsers(
                adminGuard.extractUserId(authHeader), campaignId, role));
    }

    /**
     * Assigns a coordinator to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @param request    body containing userId
     * @return {@link es.grupo8.backend.dto.AssignmentResultDTO} with 201 status, or 400 if userId is missing
     */
    @PostMapping("/{campaignId}/coordinators")
    public ResponseEntity<?> assignCoordinator(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        checkAdmin(authHeader);
        Integer userId = extractUserId(request);
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignAssignmentService.assignCoordinator(adminGuard.extractUserId(authHeader), campaignId, userId));
    }

    /**
     * Removes a coordinator from a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @param userId     coordinator user identifier
     * @return success message
     */
    @DeleteMapping("/{campaignId}/coordinators/{userId}")
    public ResponseEntity<?> unassignCoordinator(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer userId) {

        checkAdmin(authHeader);
        campaignAssignmentService.unassignCoordinator(adminGuard.extractUserId(authHeader), campaignId, userId);
        return ResponseEntity.ok(Map.of("message", "Coordinator unassigned successfully"));
    }

    /**
     * Assigns a captain to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @param request    body containing userId
     * @return {@link es.grupo8.backend.dto.AssignmentResultDTO} with 201 status, or 400 if userId is missing
     */
    @PostMapping("/{campaignId}/captains")
    public ResponseEntity<?> assignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        checkAdmin(authHeader);
        Integer userId = extractUserId(request);
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignAssignmentService.assignCaptain(adminGuard.extractUserId(authHeader), campaignId, userId));
    }

    /**
     * Removes a captain from a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId target campaign identifier
     * @param userId     captain user identifier
     * @return success message
     */
    @DeleteMapping("/{campaignId}/captains/{userId}")
    public ResponseEntity<?> unassignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer userId) {

        checkAdmin(authHeader);
        campaignAssignmentService.unassignCaptain(adminGuard.extractUserId(authHeader), campaignId, userId);
        return ResponseEntity.ok(Map.of("message", "Captain unassigned successfully"));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e validation error */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    /** @param e entity not found */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /** @param e conflict / already-assigned */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static Integer extractUserId(Map<String, Object> request) {
        if (request == null) return null;
        Object raw = request.get("userId");
        if (raw instanceof Integer i) return i;
        if (raw instanceof Number n) return n.intValue();
        if (raw instanceof String s) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) return null;
            try { return Integer.valueOf(trimmed); } catch (NumberFormatException ex) { return null; }
        }
        return null;
    }
}
