package es.grupo8.backend.controllers;

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

import es.grupo8.backend.security.CaptainGuard;
import es.grupo8.backend.services.CaptainDashboardService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/captain")
@AllArgsConstructor
public class CaptainDashboardController {

    private final CaptainGuard captainGuard;
    private final CaptainDashboardService captainDashboardService;

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();
        return ResponseEntity.ok(captainDashboardService.getMyCampaigns(captainGuard.extractUserId(authHeader)));
    }

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();
        return ResponseEntity.ok(captainDashboardService.getMyStores(
                captainGuard.extractUserId(authHeader), campaignId));
    }

    @GetMapping("/shifts")
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();
        return ResponseEntity.ok(captainDashboardService.getShifts(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    @GetMapping("/volunteer-shifts")
    public ResponseEntity<?> getVolunteerShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();
        return ResponseEntity.ok(captainDashboardService.getVolunteerShifts(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();

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

    @GetMapping("/incidents")
    public ResponseEntity<?> getIncidents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) return forbidden();
        return ResponseEntity.ok(captainDashboardService.getIncidents(
                captainGuard.extractUserId(authHeader), campaignId, storeId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acceso denegado"));
    }

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
