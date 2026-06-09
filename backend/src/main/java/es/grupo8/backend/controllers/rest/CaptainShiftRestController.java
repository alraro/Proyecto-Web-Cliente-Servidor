package es.grupo8.backend.controllers.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.security.CaptainGuard;
import es.grupo8.backend.services.CaptainShiftService;
import lombok.AllArgsConstructor;

/**
 * REST controller for captain team-shift queries.
 * The /my-team endpoint is captain-exclusive within the /api/shifts namespace.
 */
@RestController
@RequestMapping("/api/shifts")
@AllArgsConstructor
public class CaptainShiftRestController {

    private final CaptainGuard captainGuard;
    private final CaptainShiftService captainShiftService;

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
     * Returns all shift assignments for the captain's team in a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @return list of team shift maps
     */
    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeamShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        checkCaptain(authHeader);
        return ResponseEntity.ok(captainShiftService.getMyTeamShifts(
                captainGuard.extractUserId(authHeader), campaignId));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e validation error */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
