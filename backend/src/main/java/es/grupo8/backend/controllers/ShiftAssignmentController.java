package es.grupo8.backend.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.ShiftCaptainRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.ShiftCaptain;
import es.grupo8.backend.entity.ShiftCaptainId;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;
import es.grupo8.backend.entity.CampaignStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/shifts/{shiftId}")
@Tag(name = "Asignación a turnos", description = "Asignar y desasignar voluntarios y capitanes a turnos concretos")
@SecurityRequirement(name = "Bearer Authentication")
public class ShiftAssignmentController {

    @Autowired private ShiftRepository            shiftRepository;
    @Autowired private VolunteerRepository         volunteerRepository;
    @Autowired private VolunteerShiftRepository    volunteerShiftRepository;
    @Autowired private ShiftCaptainRepository      shiftCaptainRepository;
    @Autowired private UserRepository              userRepository;
    @Autowired private CampaignStoreRepository     campaignStoreRepository;

    // ── Voluntarios ───────────────────────────────────────────────────────────

    @Operation(summary = "Listar voluntarios asignados al turno")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de voluntarios"),
        @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        List<VolunteerShift> assignments = volunteerShiftRepository.findByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime());

        List<Map<String, Object>> result = assignments.stream()
                .map(vs -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("volunteerId", vs.getIdVolunteer().getId());
                    m.put("name",        vs.getIdVolunteer().getName());
                    m.put("email",       vs.getIdVolunteer().getEmail());
                    m.put("phone",       vs.getIdVolunteer().getPhone());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "shiftId",          shiftId,
                "volunteersNeeded", shift.getVolunteersNeeded(),
                "volunteersAssigned", result.size(),
                "volunteers",       result
        ));
    }

    @Operation(summary = "Asignar voluntario al turno",
               description = "Valida aforo máximo (RF-28) y solapamiento de horarios (RF-28) antes de asignar.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Voluntario asignado"),
        @ApiResponse(responseCode = "400", description = "Aforo completo o solapamiento detectado"),
        @ApiResponse(responseCode = "404", description = "Turno o voluntario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Voluntario ya asignado a este turno")
    })
    @PostMapping("/volunteers")
    public ResponseEntity<?> assignVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @RequestBody(required = false) Map<String, Object> request) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        Integer volunteerId = parseInteger(request == null ? null : request.get("volunteerId"));
        if (volunteerId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "volunteerId es obligatorio"));
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId).orElse(null);
        if (volunteer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Voluntario no encontrado"));
        }

        // Comprobar si ya está asignado
        VolunteerShiftId vsId = buildVolunteerShiftId(volunteerId, shift);
        if (volunteerShiftRepository.existsById(vsId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El voluntario ya está asignado a este turno"));
        }

        // RF-28: Validar aforo máximo
        long currentCount = volunteerShiftRepository.countByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime());

        if (currentCount >= shift.getVolunteersNeeded()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Aforo completo: este turno ya tiene el máximo de voluntarios (" +
                               shift.getVolunteersNeeded() + ")",
                    "conflict", "CAPACITY_EXCEEDED"
            ));
        }

        // RF-28: Validar solapamiento de turnos para este voluntario
        List<VolunteerShift> overlaps = volunteerShiftRepository.findOverlappingForVolunteer(
                volunteerId,
                shift.getShiftDay(),
                shift.getStartTime(),
                shift.getEndTime());

        if (!overlaps.isEmpty()) {
            VolunteerShift overlap = overlaps.get(0);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "El voluntario ya tiene un turno solapado ese día: " +
                               overlap.getId().getStartTime() + " – " + overlap.getEndTime(),
                    "conflict", "OVERLAP"
            ));
        }

        // Obtener CampaignStore necesario para la entidad VolunteerShift
        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(shift.getIdCampaign().getId());
        csId.setIdStore(shift.getIdStore().getId());
        CampaignStore campaignStore = campaignStoreRepository.findById(csId).orElse(null);
        if (campaignStore == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La tienda ya no está asociada a esta campaña"));
        }

        VolunteerShift vs = new VolunteerShift();
        vs.setId(vsId);
        vs.setIdVolunteer(volunteer);
        vs.setCampaignStores(campaignStore);
        vs.setEndTime(shift.getEndTime());
        vs.setAttendance(false);
        volunteerShiftRepository.save(vs);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",      "Voluntario asignado correctamente",
                "shiftId",      shiftId,
                "volunteerId",  volunteerId,
                "volunteerName", volunteer.getName()
        ));
    }

    @Operation(summary = "Desasignar voluntario del turno")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Voluntario desasignado"),
        @ApiResponse(responseCode = "404", description = "Turno o asignación no encontrada")
    })
    @DeleteMapping("/volunteers/{volunteerId}")
    public ResponseEntity<?> unassignVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @PathVariable Integer volunteerId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        VolunteerShiftId vsId = buildVolunteerShiftId(volunteerId, shift);
        if (!volunteerShiftRepository.existsById(vsId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "El voluntario no está asignado a este turno"));
        }

        volunteerShiftRepository.deleteById(vsId);
        return ResponseEntity.ok(Map.of("message", "Voluntario desasignado correctamente"));
    }

    // ── Capitanes ─────────────────────────────────────────────────────────────

    @Operation(summary = "Listar capitanes asignados al turno")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de capitanes"),
        @ApiResponse(responseCode = "404", description = "Turno no encontrado")
    })
    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        List<Map<String, Object>> result = shiftCaptainRepository.findByShift_Id(shiftId)
                .stream()
                .map(sc -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", sc.getUser().getIdUser());
                    m.put("name",   sc.getUser().getName());
                    m.put("email",  sc.getUser().getEmail());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("shiftId", shiftId, "captains", result));
    }

    @Operation(summary = "Asignar capitán al turno",
               description = "Valida solapamiento de horarios (RF-28) antes de asignar.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Capitán asignado"),
        @ApiResponse(responseCode = "400", description = "Solapamiento detectado"),
        @ApiResponse(responseCode = "404", description = "Turno o usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "Capitán ya asignado a este turno")
    })
    @PostMapping("/captains")
    public ResponseEntity<?> assignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @RequestBody(required = false) Map<String, Object> request) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        Integer userId = parseInteger(request == null ? null : request.get("userId"));
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "userId es obligatorio"));
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado"));
        }

        // Comprobar si ya está asignado
        ShiftCaptainId scId = new ShiftCaptainId();
        scId.setIdShift(shiftId);
        scId.setIdUser(userId);
        if (shiftCaptainRepository.existsById(scId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El capitán ya está asignado a este turno"));
        }

        // RF-28: Validar solapamiento de turnos para este capitán
        List<ShiftCaptain> overlaps = shiftCaptainRepository.findOverlappingForCaptain(
                userId,
                shift.getShiftDay(),
                shift.getStartTime(),
                shift.getEndTime(),
                shiftId);

        if (!overlaps.isEmpty()) {
            ShiftCaptain overlap = overlaps.get(0);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "El capitán ya tiene un turno solapado ese día: " +
                               overlap.getShift().getStartTime() + " – " + overlap.getShift().getEndTime(),
                    "conflict", "OVERLAP"
            ));
        }

        ShiftCaptain sc = new ShiftCaptain();
        sc.setId(scId);
        sc.setShift(shift);
        sc.setUser(user);
        shiftCaptainRepository.save(sc);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",  "Capitán asignado correctamente",
                "shiftId",  shiftId,
                "userId",   userId,
                "userName", user.getName()
        ));
    }

    @Operation(summary = "Desasignar capitán del turno")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Capitán desasignado"),
        @ApiResponse(responseCode = "404", description = "Turno o asignación no encontrada")
    })
    @DeleteMapping("/captains/{userId}")
    public ResponseEntity<?> unassignCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId,
            @PathVariable Integer userId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        ShiftCaptainId scId = new ShiftCaptainId();
        scId.setIdShift(shiftId);
        scId.setIdUser(userId);
        if (!shiftCaptainRepository.existsById(scId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "El capitán no está asignado a este turno"));
        }

        shiftCaptainRepository.deleteById(scId);
        return ResponseEntity.ok(Map.of("message", "Capitán desasignado correctamente"));
    }

    // ── Listas para desplegables ──────────────────────────────────────────────

    @Operation(summary = "Voluntarios disponibles para asignar al turno")
    @GetMapping("/available-volunteers")
    public ResponseEntity<?> getAvailableVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        // Ya asignados a este turno
        List<Integer> alreadyAssigned = volunteerShiftRepository.findByShift(
                shift.getIdCampaign().getId(),
                shift.getIdStore().getId(),
                shift.getShiftDay(),
                shift.getStartTime())
                .stream()
                .map(vs -> vs.getIdVolunteer().getId())
                .collect(Collectors.toList());

        List<Map<String, Object>> result = volunteerRepository.findAll().stream()
                .filter(v -> !alreadyAssigned.contains(v.getId()))
                .map(v -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("volunteerId", v.getId());
                    m.put("name",        v.getName());
                    m.put("email",       v.getEmail());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Capitanes disponibles para asignar al turno")
    @GetMapping("/available-captains")
    public ResponseEntity<?> getAvailableCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer shiftId) {

        Shift shift = shiftRepository.findById(shiftId).orElse(null);
        if (shift == null) return shiftNotFound();

        // Ya asignados a este turno
        List<Integer> alreadyAssigned = shiftCaptainRepository.findByShift_Id(shiftId)
                .stream()
                .map(sc -> sc.getUser().getIdUser())
                .collect(Collectors.toList());

        List<Map<String, Object>> result = userRepository.findAllCaptains().stream()
                .filter(u -> !alreadyAssigned.contains(u.getIdUser()))
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", u.getIdUser());
                    m.put("name",   u.getName());
                    m.put("email",  u.getEmail());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private VolunteerShiftId buildVolunteerShiftId(Integer volunteerId, Shift shift) {
        VolunteerShiftId id = new VolunteerShiftId();
        id.setIdVolunteer(volunteerId);
        id.setIdCampaign(shift.getIdCampaign().getId());
        id.setIdStore(shift.getIdStore().getId());
        id.setShiftDay(shift.getShiftDay());
        id.setStartTime(shift.getStartTime());
        return id;
    }

    private ResponseEntity<?> shiftNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Turno no encontrado"));
    }

    private static Integer parseInteger(Object value) {
        if (value == null) return null;
        try { return Integer.valueOf(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
