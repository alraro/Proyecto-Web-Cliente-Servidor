/**
 * Controlador REST del panel del capitán (campañas, tiendas, turnos, incidencias).
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 75%
 * - Alfonso Ramos Rojas: 5%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CaptainDashboardService;
import es.grupo8.backend.services.UserService;
import es.grupo8.backend.util.BodyParams;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/captain")
@AllArgsConstructor
public class CaptainDashboardRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CaptainDashboardService captainDashboardService;

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainDashboardService.getMyCampaigns(authService.extractUserIdFromToken(authHeader)));
    }

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainDashboardService.getMyStores(
                authService.extractUserIdFromToken(authHeader), campaignId));
    }

    @GetMapping("/shifts")
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainDashboardService.getShifts(
                authService.extractUserIdFromToken(authHeader), campaignId, storeId));
    }

    @GetMapping("/volunteer-shifts")
    public ResponseEntity<?> getVolunteerShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainDashboardService.getVolunteerShifts(
                authService.extractUserIdFromToken(authHeader), campaignId, storeId));
    }

    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        Integer campaignId  = BodyParams.toInteger(request.get("campaignId"));
        Integer storeId     = BodyParams.toInteger(request.get("storeId"));
        String  description = BodyParams.trimToNull(request.get("description"));
        Integer userId      = authService.extractUserIdFromToken(authHeader);

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

        if (!userService.isCaptainFromToken(authHeader)) return forbidden("captains");
        return ResponseEntity.ok(captainDashboardService.getIncidents(
                authService.extractUserIdFromToken(authHeader), campaignId, storeId));
    }
}
