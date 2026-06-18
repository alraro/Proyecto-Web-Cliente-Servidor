/**
 * Controlador MVC para la asignación de turnos.
 *
 * Autores:
 * - Alejandro Calvo Aguilar: 75%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.AttendanceRequestDto;
import es.grupo8.backend.dto.CaptainShiftAssignRequestDto;
import es.grupo8.backend.dto.VolunteerShiftAssignRequestDto;
import es.grupo8.backend.exceptions.ShiftConflictException;
import es.grupo8.backend.services.ShiftAssignmentService;
import es.grupo8.backend.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/shifts/{shiftId}")
@Tag(name = "Asignación a turnos", description = "Asignar y desasignar voluntarios y capitanes a turnos concretos")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class ShiftAssignmentController {

    private final UserService userService;
    private final ShiftAssignmentService shiftAssignmentService;

    @Operation(summary = "Listar voluntarios asignados al turno")
    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(shiftAssignmentService.getVolunteers(shiftId));
    }

    @Operation(summary = "Asignar voluntario al turno",
               description = "Valida aforo máximo (RF-28) y solapamiento de horarios (RF-28) antes de asignar.")
    @PostMapping("/volunteers")
    public ResponseEntity<?> assignVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @RequestBody(required = false) VolunteerShiftAssignRequestDto request) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftAssignmentService.assignVolunteer(shiftId, request));
    }

    @Operation(summary = "Desasignar voluntario del turno")
    @DeleteMapping("/volunteers/{volunteerId}")
    public ResponseEntity<?> unassignVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @PathVariable Integer volunteerId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        shiftAssignmentService.unassignVolunteer(shiftId, volunteerId);
        return ResponseEntity.ok(Map.of("message", "Voluntario desasignado correctamente"));
    }

    @Operation(summary = "Listar capitanes asignados al turno")
    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(shiftAssignmentService.getCaptains(shiftId));
    }

    @Operation(summary = "Asignar capitán al turno",
               description = "Valida solapamiento de horarios (RF-28) antes de asignar.")
    @PostMapping("/captains")
    public ResponseEntity<?> assignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @RequestBody(required = false) CaptainShiftAssignRequestDto request) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shiftAssignmentService.assignCaptain(shiftId, request));
    }

    @Operation(summary = "Desasignar capitán del turno")
    @DeleteMapping("/captains/{userId}")
    public ResponseEntity<?> unassignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @PathVariable Integer userId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        shiftAssignmentService.unassignCaptain(shiftId, userId);
        return ResponseEntity.ok(Map.of("message", "Capitán desasignado correctamente"));
    }

    @Operation(summary = "Registrar asistencia de un voluntario en un turno")
    @PutMapping("/attendance")
    public ResponseEntity<?> updateAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @RequestBody(required = false) AttendanceRequestDto request) {
        if (!userService.isCaptainFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(shiftAssignmentService.updateAttendance(shiftId, request));
    }

    @Operation(summary = "Voluntarios disponibles para asignar al turno")
    @GetMapping("/available-volunteers")
    public ResponseEntity<?> getAvailableVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(shiftAssignmentService.getAvailableVolunteers(shiftId));
    }

    @Operation(summary = "Capitanes disponibles para asignar al turno")
    @GetMapping("/available-captains")
    public ResponseEntity<?> getAvailableCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {
        if (!userService.isCoordinatorFromToken(authHeader)) return forbidden();
        return ResponseEntity.ok(shiftAssignmentService.getAvailableCaptains(shiftId));
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acceso denegado."));
    }

    @ExceptionHandler(ShiftConflictException.class)
    public ResponseEntity<?> handleShiftConflict(ShiftConflictException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage(), "conflict", e.getConflictType()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }
}
