package es.grupo8.backend.controllers.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.security.CaptainGuard;
import es.grupo8.backend.services.CaptainDashboardService;
import lombok.AllArgsConstructor;

/**
 * REST controller for captain dashboard operations: campaigns, stores, shifts, incidents.
 * All endpoints require a valid captain JWT.
 */
@RestController
@RequestMapping("/api/captain")
@AllArgsConstructor
public class CaptainDashboardRestController {

    private final CaptainGuard captainGuard;
    private final CaptainDashboardService captainDashboardService;

    // ── Auth helper ───────────────────────────────────────────────────────────

    /**
     * Validates the Authorization header and asserts captain role.
     *
     * @param auth raw Authorization header value
     * @throws AuthException 401 if token is missing/invalid, 403 if not a captain
     */
    private void checkCaptain(String auth) {
        if (captainGuard.extractUserId(auth) == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!captainGuard.isUserCaptain(auth)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Returns the campaigns assigned to the authenticated captain.
     *
     * @param authHeader JWT Authorization header
     * @return list of {@link es.grupo8.backend.dto.CampaignDTO}
     */
    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainDashboardService.getMyCampaigns(captainGuard.extractUserId(authHeader)));
    }

    /**
     * Returns the stores assigned to the captain for a given campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @return list of {@link es.grupo8.backend.dto.StoreDTO}
     */
    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainDashboardService.getMyStores(
                captainGuard.extractUserId(authHeader), campaignId));
    }

    /**
     * Returns shifts assigned to the captain filtered by campaign and optionally by store.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @param storeId    optional store filter
     * @return list of {@link es.grupo8.backend.dto.ShiftResponseDto}
     */
    @GetMapping("/shifts")
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainDashboardService.getShifts(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    /**
     * Returns volunteer-shift assignments for the captain's stores.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @param storeId    optional store filter
     * @return list of {@link es.grupo8.backend.dto.VolunteerShiftDTO}
     */
    @GetMapping("/volunteer-shifts")
    public ResponseEntity<?> getVolunteerShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainDashboardService.getVolunteerShifts(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    /**
     * Creates an incident report for a store in a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param request    incident data (campaignId, storeId, description)
     * @return incident identifier with 201 status
     */
    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        checkCaptain(authHeader);
        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        Integer campaignId  = parseInteger(request.get("campaignId"));
        Integer storeId     = parseInteger(request.get("storeId"));
        String  description = trimToNull(request.get("description"));
        Integer userId      = captainGuard.extractUserId(authHeader);

        Integer incidentId = captainDashboardService.createIncident(userId, campaignId, storeId, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",    "Incidencia registrada correctamente",
                "incidentId", incidentId));
    }

    /**
     * Returns incidents reported by the captain filtered by campaign and optionally by store.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @param storeId    optional store filter
     * @return list of {@link es.grupo8.backend.dto.IncidentDTO}
     */
    @GetMapping("/incidents")
    public ResponseEntity<?> getIncidents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainDashboardService.getIncidents(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e validation error */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static String trimToNull(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer parseInteger(Object o) {
        if (o == null) return null;
        try { return Integer.valueOf(o.toString()); } catch (NumberFormatException e) { return null; }
    }
}
