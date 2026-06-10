package es.grupo8.backend.controllers.rest;

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

import es.grupo8.backend.dto.CaptainRequestDto;
import es.grupo8.backend.dto.CoordinatorVolunteerShiftRequestDto;
import es.grupo8.backend.dto.VolunteerRequestDto;
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.CoordinatorDashboardService;
import es.grupo8.backend.services.UserService;
import lombok.AllArgsConstructor;

/**
 * REST controller for coordinator dashboard operations: volunteers, captains, shifts and entities.
 * All endpoints require a valid coordinator JWT.
 */
@RestController
@RequestMapping("/api/coordinator")
@AllArgsConstructor
public class CoordinatorDashboardRestController {

    private final AuthService authService;
    private final UserService userService;
    private final CoordinatorDashboardService coordinatorDashboardService;

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Returns the campaigns assigned to the authenticated coordinator.
     *
     * @param authHeader JWT Authorization header
     * @return list of {@link es.grupo8.backend.dto.CampaignDTO}
     */
    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getMyCampaigns(
                authService.extractUserIdFromToken(authHeader)));
    }

    /**
     * Returns the stores assigned to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @return list of {@link es.grupo8.backend.dto.StoreDTO}
     */
    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getMyStores(campaignId));
    }

    /**
     * Returns all volunteers.
     *
     * @param authHeader JWT Authorization header
     * @return list of {@link es.grupo8.backend.dto.VoluntarioResponseDto}
     */
    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getVolunteers());
    }

    /**
     * Creates a new volunteer.
     *
     * @param authHeader JWT Authorization header
     * @param request    volunteer creation data
     * @return created volunteer as {@link es.grupo8.backend.dto.VoluntarioResponseDto} with 201 status
     */
    @PostMapping("/volunteers")
    public ResponseEntity<?> createVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) VolunteerRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

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

    /**
     * Updates an existing volunteer.
     *
     * @param authHeader JWT Authorization header
     * @param id         volunteer identifier
     * @param request    volunteer update data
     * @return updated volunteer as {@link es.grupo8.backend.dto.VoluntarioResponseDto}
     */
    @PutMapping("/volunteers/{id}")
    public ResponseEntity<?> updateVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) VolunteerRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

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

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

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

    /**
     * Returns captains filtered by campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @return list of {@link es.grupo8.backend.dto.UserDTO} for captains
     */
    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getCaptains(campaignId));
    }

    /**
     * Registers a new captain user and assigns them to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param request    captain registration data (name, email, password, campaignId)
     * @return {@link es.grupo8.backend.dto.RegisterResultDTO} with 201 status
     */
    @PostMapping("/captains/register")
    public ResponseEntity<?> registerCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) CaptainRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();

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

    /**
     * Returns all partner entities.
     *
     * @param authHeader JWT Authorization header
     * @return list of {@link es.grupo8.backend.dto.PartnerEntityResponseDto}
     */
    @GetMapping("/partner-entities")
    public ResponseEntity<?> getPartnerEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getPartnerEntities());
    }

    /**
     * Returns partner entities that have volunteers assigned to a campaign.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId optional campaign filter
     * @return list of {@link es.grupo8.backend.dto.CampaignEntityDTO}
     */
    @GetMapping("/campaign-entities")
    public ResponseEntity<?> getCampaignEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(coordinatorDashboardService.getCampaignEntities(campaignId));
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /** @param e validation error */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    /** @param e entity not found */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    /** @param e conflict */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access restricted to coordinators"));
    }
}
