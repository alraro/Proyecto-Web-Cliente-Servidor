/**
 * Controlador REST del panel del coordinador (voluntarios, capitanes, turnos, entidades).
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 65%
 * - Alejandro Calvo Aguilar: 5%
 * - Alfonso Ramos Rojas: 5%
 * - IA Generativa: 25%
 */
package es.grupo8.backend.controllers.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.CaptainRequestDto;
import es.grupo8.backend.dto.CoordinatorVolunteerShiftRequestDto;
import es.grupo8.backend.dto.VolunteerRequestDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/coordinator")
@AllArgsConstructor
public class CoordinatorDashboardRestController extends BaseRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CoordinatorDashboardService coordinatorDashboardService;

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getMyCampaigns(
                authService.extractUserIdFromToken(authHeader)));
    }

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getMyStores(campaignId));
    }

    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getVolunteers());
    }

    @PostMapping("/volunteers")
    public ResponseEntity<?> createVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) VolunteerRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                coordinatorDashboardService.createVolunteer(
                        authService.extractUserIdFromToken(authHeader),
                        request.getName(),
                        request.getPhone(),
                        request.getEmail(),
                        request.getAddress(),
                        request.getPartnerEntityId()));
    }

    @PutMapping("/volunteers/{id}")
    public ResponseEntity<?> updateVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) VolunteerRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }
        return ResponseEntity.ok(coordinatorDashboardService.updateVolunteer(
                authService.extractUserIdFromToken(authHeader),
                id,
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                request.getAddress(),
                request.getPartnerEntityId()));
    }

    @PostMapping("/volunteer-shifts")
    public ResponseEntity<?> assignVolunteerShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CoordinatorVolunteerShiftRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }
        coordinatorDashboardService.assignVolunteerShift(
                authService.extractUserIdFromToken(authHeader),
                request.getVolunteerId(),
                request.getCampaignId(),
                request.getStoreId(),
                request.getShiftDay(),
                request.getStartTime(),
                request.getEndTime());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Voluntario asignado al turno correctamente"));
    }

    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getCaptains(campaignId));
    }

    @PostMapping("/captains/register")
    public ResponseEntity<?> registerCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CaptainRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                coordinatorDashboardService.registerCaptain(
                        authService.extractUserIdFromToken(authHeader),
                        request.getName(),
                        request.getEmail(),
                        request.getPassword(),
                        request.getCampaignId()));
    }

    @GetMapping("/partner-entities")
    public ResponseEntity<?> getPartnerEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getPartnerEntities());
    }

    @GetMapping("/campaign-entities")
    public ResponseEntity<?> getCampaignEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden("coordinators");
        return ResponseEntity.ok(coordinatorDashboardService.getCampaignEntities(campaignId));
    }

}
