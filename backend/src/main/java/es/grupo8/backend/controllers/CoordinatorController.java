package es.grupo8.backend.controllers;

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
import es.grupo8.backend.services.AuthService;
import es.grupo8.backend.services.ShiftService;
import es.grupo8.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Turnos de recogida", description = "API para gestionar turnos de recogida de alimentos")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class CoordinatorController {

    private final ShiftService shiftService;
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Crear turno de recogida", description = "Crea un nuevo turno de recogida para una campaña y tienda. Solo accesible por Coordinadores.")
    @PostMapping
    public ResponseEntity<?> createShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) ShiftRequestDto request) {

        if (!userService.isCoordinatorFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado. Solo los coordinadores pueden crear turnos."));
        }

        Integer userId = authService.extractUserIdFromToken(authHeader);
        ShiftResponseDto created = shiftService.createShift(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId", required = false) Integer storeId) {

        if (!userService.isCoordinatorFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado. Solo los coordinadores pueden ver turnos."));
        }

        List<ShiftResponseDto> shifts = shiftService.getShifts(campaignId, storeId);
        return ResponseEntity.ok(shifts);
    }

    @Operation(summary = "Tiendas de una campaña para el Coordinador",
            description = "Devuelve las tiendas asignadas a una campaña para que el coordinador pueda seleccionarla al crear un turno.")
    @GetMapping("/campaign/{campaignId}/stores")
    public ResponseEntity<?> getStoresForCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer campaignId) {

        List<StoreSimpleDto> stores = shiftService.getStoresForCampaign(campaignId);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "Calendario de turnos por tienda, día y franja horaria",
               description = "Devuelve los turnos agrupados por tienda → día → franja para el panel visual.")
    @GetMapping("/calendar")
    public ResponseEntity<?> getCalendar(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!userService.isCoordinatorFromToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado. Solo los coordinadores pueden ver el calendario."));
        }

        List<ShiftCalendarStoreDto> calendar = shiftService.getCalendar(campaignId);
        return ResponseEntity.ok(calendar);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }
}
