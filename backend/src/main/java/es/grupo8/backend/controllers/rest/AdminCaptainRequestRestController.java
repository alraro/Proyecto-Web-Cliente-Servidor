/**
 * Controlador REST para gestionar solicitudes de alta de capitanes.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.services.AdminCaptainRequestService;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

// Admin API to list, approve and reject captain sign-up requests.
// The controller only checks the token and shapes the response; the real work lives in the service.
@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminCaptainRequestRestController {

    private final AuthService authService;
    private final UserService userService;
    private final AdminCaptainRequestService adminCaptainRequestService;

    // ── Endpoints ─────────────────────────────────────────────────────────────

    // Lists requests, pending by default. Returns one flat map per request.
    @GetMapping("/captain-requests")
    public ResponseEntity<?> getCaptainRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "status", defaultValue = "PENDIENTE") String status) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        List<Map<String, Object>> result = adminCaptainRequestService.getRequests(status).stream()
                .map(this::requestToMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Approving creates the user, grants the captain role and closes the request — all inside the service.
    @PostMapping("/captain-requests/{id}/approve")
    public ResponseEntity<?> approveCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        Integer newUserId = adminCaptainRequestService.approveRequest(
                authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of(
                "message", "Capitán aprobado y creado correctamente.",
                "userId",  newUserId));
    }

    // Rejecting just flips the request's status.
    @PostMapping("/captain-requests/{id}/reject")
    public ResponseEntity<?> rejectCaptainRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id) {

        if (!userService.isAdminFromToken(authHeader)) return forbidden();
        adminCaptainRequestService.rejectRequest(authService.extractUserIdFromToken(authHeader), id);
        return ResponseEntity.ok(Map.of("message", "Solicitud rechazada."));
    }

    // ── Error handling ──────────────────────────────────────────────────────────

    /** @param e request not found */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /** @param e request already processed */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access restricted to administrators"));
    }

    // Flattens the entity into a simple map for the JSON response (with campaign and coordinator info).
    private Map<String, Object> requestToMap(CaptainRequest r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",              r.getId());
        m.put("name",            r.getName());
        m.put("email",           r.getEmail());
        m.put("status",          r.getStatus());
        m.put("createdAt",       r.getCreatedAt()  != null ? r.getCreatedAt().toString()  : null);
        m.put("resolvedAt",      r.getResolvedAt() != null ? r.getResolvedAt().toString() : null);
        m.put("campaignId",      r.getIdCampaign()    != null ? r.getIdCampaign().getId()       : null);
        m.put("campaignName",    r.getIdCampaign()    != null ? r.getIdCampaign().getName()     : null);
        m.put("coordinatorName", r.getIdCoordinator() != null ? r.getIdCoordinator().getName()  : null);
        return m;
    }
}
