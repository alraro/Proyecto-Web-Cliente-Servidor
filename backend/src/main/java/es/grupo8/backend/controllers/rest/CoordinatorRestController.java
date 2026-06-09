package es.grupo8.backend.controllers.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.ShiftCalendarStoreDto;
import es.grupo8.backend.dto.ShiftRequestDto;
import es.grupo8.backend.dto.ShiftResponseDto;
import es.grupo8.backend.dto.StoreSimpleDto;
import es.grupo8.backend.exceptions.AuthException;
import es.grupo8.backend.security.CoordinatorGuard;
import es.grupo8.backend.services.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

/**
 * REST controller for shift management by coordinators.
 * All write and read operations require a valid coordinator JWT.
 */
@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Turnos de recogida", description = "API para gestionar turnos de recogida de alimentos")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class CoordinatorRestController {

    private final ShiftService shiftService;
    private final CoordinatorGuard coordinatorGuard;

    // ── Auth helper ───────────────────────────────────────────────────────────

    /**
     * Validates the Authorization header and asserts coordinator role.
     *
     * @param auth raw Authorization header value
     * @throws AuthException 401 if token is missing/invalid, 403 if not a coordinator
     */
    private void checkCoordinator(String auth) {
        if (coordinatorGuard.extractUserId(auth) == null) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Token inválido o ausente");
        }
        if (!coordinatorGuard.isCoordinator(auth)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Acceso denegado. Solo los coordinadores pueden realizar esta acción.");
        }
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Creates a new pickup shift for a campaign and store.
     * Only accessible by coordinators (RNF-03). Creates an audit log on creation (RNF-15).
     *
     * @param authHeader JWT Authorization header
     * @param request    shift creation data
     * @return created shift DTO with 201 status
     */
    @Operation(
            summary = "Crear turno de recogida",
            description = "Crea un nuevo turno de recogida para una campaña y tienda. Solo accesible por Coordinadores.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShiftResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o campaña/tienda no encontrada"),
            @ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Solo para coordinadores."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ShiftRequestDto request) {

        checkCoordinator(authHeader);
        Integer userId = coordinatorGuard.extractUserId(authHeader);
        ShiftResponseDto created = shiftService.createShift(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Returns shifts for a specific campaign, optionally filtered by store.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId required campaign identifier
     * @param storeId    optional store identifier
     * @return list of shift response DTOs
     */
    @GetMapping
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId", required = false) Integer storeId) {

        checkCoordinator(authHeader);
        List<ShiftResponseDto> shifts = shiftService.getShifts(campaignId, storeId);
        return ResponseEntity.ok(shifts);
    }

    /**
     * Returns stores assigned to a campaign so the coordinator can select one when creating a shift.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId campaign identifier
     * @return list of simple store DTOs sorted by name
     */
    @Operation(
            summary = "Tiendas de una campaña para el Coordinador",
            description = "Devuelve las tiendas asignadas a una campaña para que el coordinador pueda seleccionarla al crear un turno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de tiendas de la campaña"),
            @ApiResponse(responseCode = "404", description = "Campaña no encontrada")
    })
    @GetMapping("/campaign/{campaignId}/stores")
    public ResponseEntity<?> getStoresForCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        List<StoreSimpleDto> stores = shiftService.getStoresForCampaign(campaignId);
        return ResponseEntity.ok(stores);
    }

    /**
     * Returns the shift calendar for a campaign grouped by store → day → time slot.
     * Optimised for RNF-06 with only two database queries.
     *
     * @param authHeader JWT Authorization header
     * @param campaignId required campaign identifier
     * @return list of calendar store DTOs
     */
    @Operation(
            summary = "Calendario de turnos por tienda, día y franja horaria",
            description = "Devuelve los turnos agrupados por tienda → día → franja para el panel visual.")
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        checkCoordinator(authHeader);
        List<ShiftCalendarStoreDto> calendar = shiftService.getCalendar(campaignId);
        return ResponseEntity.ok(calendar);
    }

    // ── Local exception handlers ──────────────────────────────────────────────

    /**
     * Handles validation and business rule errors returning HTTP 400.
     *
     * @param e the exception thrown by the service
     * @return error response with the exception message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }
}
