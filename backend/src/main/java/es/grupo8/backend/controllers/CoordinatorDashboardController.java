package es.grupo8.backend.controllers;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/coordinator")
@AllArgsConstructor
public class CoordinatorDashboardController {

    private final AuthService authService;
    private final UserService userService;
    private final CoordinatorDashboardService coordinatorDashboardService;

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getMyCampaigns(
                authService.extractUserIdFromToken(authHeader)));
    }

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getMyStores(campaignId));
    }

    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getVolunteers());
    }

    @PostMapping("/volunteers")
    public ResponseEntity<?> createVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                coordinatorDashboardService.createVolunteer(
                        authService.extractUserIdFromToken(authHeader),
                        trimToNull(request.get("name")),
                        trimToNull(request.get("phone")),
                        trimToNull(request.get("email")),
                        trimToNull(request.get("address")),
                        parseInteger(request.get("partnerEntityId"))));
    }

    @PutMapping("/volunteers/{id}")
    public ResponseEntity<?> updateVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        return ResponseEntity.ok(coordinatorDashboardService.updateVolunteer(
                authService.extractUserIdFromToken(authHeader),
                id,
                trimToNull(request.get("name")),
                trimToNull(request.get("phone")),
                trimToNull(request.get("email")),
                trimToNull(request.get("address")),
                request.containsKey("partnerEntityId"),
                parseInteger(request.get("partnerEntityId"))));
    }

    @PostMapping("/volunteer-shifts")
    public ResponseEntity<?> assignVolunteerShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        coordinatorDashboardService.assignVolunteerShift(
                authService.extractUserIdFromToken(authHeader),
                parseInteger(request.get("volunteerId")),
                parseInteger(request.get("campaignId")),
                parseInteger(request.get("storeId")),
                trimToNull(request.get("shiftDay")),
                trimToNull(request.get("startTime")),
                trimToNull(request.get("endTime")));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Voluntario asignado al turno correctamente"));
    }

    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getCaptains(campaignId));
    }

    @PostMapping("/captains/register")
    public ResponseEntity<?> registerCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                coordinatorDashboardService.registerCaptain(
                        authService.extractUserIdFromToken(authHeader),
                        trimToNull(request.get("name")),
                        trimToNull(request.get("email")),
                        trimToNull(request.get("password")),
                        parseInteger(request.get("campaignId"))));
    }

    @GetMapping("/partner-entities")
    public ResponseEntity<?> getPartnerEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getPartnerEntities());
    }

    @GetMapping("/campaign-entities")
    public ResponseEntity<?> getCampaignEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getCampaignEntities(campaignId));
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
