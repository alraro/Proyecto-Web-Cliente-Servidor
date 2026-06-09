package es.grupo8.backend.controllers;

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

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CampaignAssignmentService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/campaigns")
@AllArgsConstructor
public class CampaignAssignmentController {

    private final AuthService authService;
    private final UserService userService;
    private final CampaignAssignmentService campaignAssignmentService;

    @GetMapping("/admin-list")
    public ResponseEntity<?> getCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(campaignAssignmentService.getCampaigns(authService.extractUserIdFromToken(authHeader)));
    }

    @GetMapping("/{campaignId}/assignments")
    public ResponseEntity<?> getCampaignAssignments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(campaignAssignmentService.getCampaignAssignments(
                authService.extractUserIdFromToken(authHeader), campaignId));
    }

    @GetMapping("/{campaignId}/available-users")
    public ResponseEntity<?> getAvailableUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestParam(value = "role", required = false) String role) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(campaignAssignmentService.getAvailableUsers(
                authService.extractUserIdFromToken(authHeader), campaignId, role));
    }

    @PostMapping("/{campaignId}/coordinators")
    public ResponseEntity<?> assignCoordinator(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        Integer userId = extractUserId(request);
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignAssignmentService.assignCoordinator(authService.extractUserIdFromToken(authHeader), campaignId, userId));
    }

    @DeleteMapping("/{campaignId}/coordinators/{userId}")
    public ResponseEntity<?> unassignCoordinator(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer userId) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        campaignAssignmentService.unassignCoordinator(authService.extractUserIdFromToken(authHeader), campaignId, userId);
        return ResponseEntity.ok(Map.of("message", "Coordinator unassigned successfully"));
    }

    @PostMapping("/{campaignId}/captains")
    public ResponseEntity<?> assignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        Integer userId = extractUserId(request);
        if (userId == null) return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                campaignAssignmentService.assignCaptain(authService.extractUserIdFromToken(authHeader), campaignId, userId));
    }

    @DeleteMapping("/{campaignId}/captains/{userId}")
    public ResponseEntity<?> unassignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId,
            @PathVariable Integer userId) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        campaignAssignmentService.unassignCaptain(authService.extractUserIdFromToken(authHeader), campaignId, userId);
        return ResponseEntity.ok(Map.of("message", "Captain unassigned successfully"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
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
