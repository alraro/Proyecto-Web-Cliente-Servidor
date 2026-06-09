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

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CaptainShiftService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

/**
 * REST controller for captain team-shift queries.
 * The /my-team endpoint is captain-exclusive within the /api/shifts namespace.
 */
@RestController
@RequestMapping("/api/shifts")
@AllArgsConstructor
public class CaptainShiftRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CaptainShiftService captainShiftService;

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

        if (!userService.isCaptainFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(captainShiftService.getMyTeamShifts(
                authService.extractUserIdFromToken(authHeader), campaignId));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e validation error */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access restricted to captains"));
    }
}
