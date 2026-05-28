package es.grupo8.backend.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.security.CaptainGuard;
import es.grupo8.backend.services.CaptainShiftService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/shifts")
@AllArgsConstructor
public class CaptainShiftController {

    private final CaptainGuard captainGuard;
    private final CaptainShiftService captainShiftService;

    @GetMapping("/my-team")
    public ResponseEntity<?> getMyTeamShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
        }

        return ResponseEntity.ok(captainShiftService.getMyTeamShifts(
                captainGuard.extractUserId(authHeader), campaignId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
