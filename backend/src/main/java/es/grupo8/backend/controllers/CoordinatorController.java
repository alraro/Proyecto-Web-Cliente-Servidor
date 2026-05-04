package es.grupo8.backend.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.security.CoordinatorGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Turnos de recogida", description = "API para gestionar turnos de recogida de alimentos")
@SecurityRequirement(name = "Bearer Authentication")
public class CoordinatorController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CoordinatorGuard coordinatorGuard;

    /**
     * Create a new pickup shift for a campaign and store.
     * Only accessible by Coordinator (RNF-03).
     * Creates audit log on creation (RNF-15).
     */
    @Operation(summary = "Crear turno de recogida", description = "Crea un nuevo turno de recogida para una campaña y tienda. Solo accesible por Coordinadores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turno creado correctamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o campaña/tienda no encontrada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. Solo para coordinadores."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> createShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        // RNF-03: Verify Coordinator role
        if (!coordinatorGuard.isCoordinator(authHeader)) {
            auditLog.warn("ACTION=CREATE_SHIFT_ATTEMPT userId={} timestamp={} reason=NOT_COORDINATOR",
                    coordinatorGuard.extractUserId(authHeader), Instant.now());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado. Solo los coordinadores pueden crear turnos."));
        }

        Integer userId = coordinatorGuard.extractUserId(authHeader);

        // Extract and validate required fields
        Object campaignIdObj = request == null ? null : request.get("campaignId");
        Object storeIdObj = request == null ? null : request.get("storeId");
        Object dayObj = request == null ? null : request.get("day");
        Object startTimeObj = request == null ? null : request.get("startTime");
        Object endTimeObj = request == null ? null : request.get("endTime");
        Object volunteersObj = request == null ? null : request.get("volunteersNeeded");
        Object locationObj = request == null ? null : request.get("location");
        // Accept both "observations" and "notes" field names
        Object observationsObj = request == null ? null : request.get("observations");
        if (observationsObj == null) {
            observationsObj = request == null ? null : request.get("notes");
        }

        // Validate required fields
        if (campaignIdObj == null || storeIdObj == null || dayObj == null ||
                startTimeObj == null || endTimeObj == null || volunteersObj == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Campos obligatorios: campaignId, storeId, day, startTime, endTime, volunteersNeeded"));
        }

        // Parse campaign ID
        Integer campaignId;
        Integer storeId;
        Integer volunteersNeeded;
        try {
            campaignId = Integer.valueOf(campaignIdObj.toString());
            storeId = Integer.valueOf(storeIdObj.toString());
            volunteersNeeded = Integer.valueOf(volunteersObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", " campaignId, storeId y volunteersNeeded deben ser numeros validos"));
        }

        // Parse and validate day
        LocalDate day;
        try {
            day = LocalDate.parse(dayObj.toString());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de fecha invalido. Use YYYY-MM-DD"));
        }

        // Parse and validate times
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(startTimeObj.toString());
            endTime = LocalTime.parse(endTimeObj.toString());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de hora invalido. Use HH:mm"));
        }

        // Validate time consistency: start < end
        if (!startTime.isBefore(endTime)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La hora de inicio debe ser anterior a la hora de fin"));
        }

        // Validate volunteers needed
        if (volunteersNeeded <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El numero de voluntarios debe ser mayor que 0"));
        }

        // Validate campaign exists
        Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
        if (campaign == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La campana no existe"));
        }

        // Validate store exists
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La tienda no existe"));
        }

        // Validate day is within campaign dates
        if (day.isBefore(campaign.getStartDate()) || day.isAfter(campaign.getEndDate())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El dia debe estar dentro del rango de la campana (" +
                            campaign.getStartDate() + " a " + campaign.getEndDate() + ")"));
        }

        // Validate store is part of campaign
        if (!campaign.getStores().contains(store)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La tienda no esta asociada a esta campana"));
        }

        // Create the shift
        Shift shift = new Shift();
        shift.setIdCampaign(campaign);
        shift.setIdStore(store);
        shift.setShiftDay(day);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);
        shift.setVolunteersNeeded(volunteersNeeded);
        shift.setLocation(locationObj != null ? locationObj.toString() : null);
        shift.setObservations(observationsObj != null ? observationsObj.toString() : null);
        shift.setCreatedBy(userId);

        Shift savedShift = shiftRepository.save(shift);

        // RNF-15: Audit log
        auditLog.info("ACTION=CREATE_SHIFT userId={} timestamp={} shiftId={} campaignId={} storeId={} day={} startTime={} endTime={}",
                userId, Instant.now(), savedShift.getId(), campaignId, storeId, day, startTime, endTime);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Turno creado correctamente",
                "shiftId", savedShift.getId(),
                "campaignId", savedShift.getIdCampaign().getId(),
                "storeId", savedShift.getIdStore().getId(),
                "day", savedShift.getShiftDay(),
                "startTime", savedShift.getStartTime(),
                "endTime", savedShift.getEndTime(),
                "volunteersNeeded", savedShift.getVolunteersNeeded(),
                "location", savedShift.getLocation() != null ? savedShift.getLocation() : "",
                "observations", savedShift.getObservations() != null ? savedShift.getObservations() : ""
        ));
    }

    /**
     * Get shifts for a specific campaign.
     * Only accessible by Coordinator.
     */
    @GetMapping
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId", required = false) Integer storeId) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Acceso denegado. Solo los coordinadores pueden ver turnos."));
        }

        List<Shift> shifts;
        if (campaignId != null && storeId != null) {
            shifts = shiftRepository.findByCampaignAndStore(campaignId, storeId);
        } else if (campaignId != null) {
            shifts = shiftRepository.findByIdCampaign(campaignId);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Proporcione campaignId para obtener los turnos"));
        }

        List<Map<String, Object>> result = shifts.stream()
                .map(this::shiftToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private Map<String, Object> shiftToMap(Shift shift) {
        Map<String, Object> map = new HashMap<>();
        map.put("shiftId", shift.getId());
        map.put("campaignId", shift.getIdCampaign().getId());
        map.put("campaignName", shift.getIdCampaign().getName());
        map.put("storeId", shift.getIdStore().getId());
        map.put("storeName", shift.getIdStore().getName());
        map.put("day", shift.getShiftDay());
        map.put("startTime", shift.getStartTime());
        map.put("endTime", shift.getEndTime());
        map.put("volunteersNeeded", shift.getVolunteersNeeded());
        map.put("location", shift.getLocation() != null ? shift.getLocation() : "");
        map.put("observations", shift.getObservations() != null ? shift.getObservations() : "");
        return map;
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acceso denegado"));
    }

    private static String trimToNull(String s) {
        return s == null ? null : s.trim().isEmpty() ? null : s.trim();
    }
}