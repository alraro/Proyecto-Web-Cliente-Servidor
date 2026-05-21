package es.grupo8.backend.controllers;

import java.time.Instant;
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

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.IncidentRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.Incident;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.security.CaptainGuard;

@RestController
@RequestMapping("/api/captain")
public class CaptainDashboardController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private CaptainGuard            captainGuard;
    @Autowired private CaptainRepository       captainRepository;
    @Autowired private CampaignStoreRepository campaignStoreRepository;
    @Autowired private ShiftRepository         shiftRepository;
    @Autowired private VolunteerShiftRepository volunteerShiftRepository;
    @Autowired private IncidentRepository      incidentRepository;

    // ── GET /api/captain/my-campaigns ────────────────────────────────────────

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        Integer userId = captainGuard.extractUserId(authHeader);
        List<Campaign> campaigns = captainRepository.findCampaignsByUserId(userId);

        List<Map<String, Object>> result = campaigns.stream()
                .map(this::campaignToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/captain/my-stores?campaignId=X ───────────────────────────────

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "campaignId es obligatorio"));
        }

        List<CampaignStore> campaignStores = campaignStoreRepository.findByIdCampaign_Id(campaignId);

        List<Map<String, Object>> result = campaignStores.stream()
                .map(this::storeToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/captain/shifts?campaignId=X&storeId=Y ───────────────────────

    @GetMapping("/shifts")
    public ResponseEntity<?> getShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "campaignId es obligatorio"));
        }

        List<Shift> shifts = (storeId != null)
                ? shiftRepository.findByCampaignAndStore(campaignId, storeId)
                : shiftRepository.findByIdCampaign(campaignId);

        List<Map<String, Object>> result = shifts.stream()
                .map(this::shiftToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/captain/volunteer-shifts?campaignId=X&storeId=Y ─────────────

    @GetMapping("/volunteer-shifts")
    public ResponseEntity<?> getVolunteerShifts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        if (campaignId == null || storeId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "campaignId y storeId son obligatorios"));
        }

        List<VolunteerShift> volunteerShifts = volunteerShiftRepository.findAll().stream()
                .filter(vs -> vs.getId().getIdCampaign().equals(campaignId)
                           && vs.getId().getIdStore().equals(storeId))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = volunteerShifts.stream()
                .map(this::volunteerShiftToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── POST /api/captain/incidents ───────────────────────────────────────────

    @PostMapping("/incidents")
    public ResponseEntity<?> createIncident(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        Integer userId = captainGuard.extractUserId(authHeader);

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        Integer campaignId  = parseInteger(request.get("campaignId"));
        Integer storeId     = parseInteger(request.get("storeId"));
        String  description = trimToNull(request.get("description"));

        if (campaignId == null || storeId == null || description == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Campos obligatorios: campaignId, storeId, description"));
        }

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);

        Store store = new Store();
        store.setId(storeId);

        UserEntity user = new UserEntity();
        user.setIdUser(userId);

        Incident incident = new Incident();
        incident.setIdCampaign(campaign);
        incident.setIdStore(store);
        incident.setIdUser(user);
        incident.setDescription(description);

        Incident saved = incidentRepository.save(incident);

        auditLog.info("ACTION=CREATE_INCIDENT userId={} timestamp={} incidentId={} campaignId={} storeId={}",
                userId, Instant.now(), saved.getId(), campaignId, storeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",    "Incidencia registrada correctamente",
                "incidentId", saved.getId()
        ));
    }

    // ── GET /api/captain/incidents?campaignId=X&storeId=Y ────────────────────

    @GetMapping("/incidents")
    public ResponseEntity<?> getIncidents(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId",    required = false) Integer storeId) {

        if (!captainGuard.isUserCaptain(authHeader)) {
            return forbidden();
        }

        if (campaignId == null || storeId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "campaignId y storeId son obligatorios"));
        }

        List<Incident> incidents = incidentRepository.findByCampaignAndStore(campaignId, storeId);

        List<Map<String, Object>> result = incidents.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",           i.getId());
            m.put("description",  i.getDescription());
            m.put("createdAt",    i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
            m.put("campaignName", i.getIdCampaign() != null ? i.getIdCampaign().getName() : null);
            m.put("storeName",    i.getIdStore()    != null ? i.getIdStore().getName()    : null);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acceso denegado"));
    }

    private Map<String, Object> campaignToMap(Campaign c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",        c.getId());
        m.put("name",      c.getName());
        m.put("startDate", c.getStartDate() != null ? c.getStartDate().toString() : null);
        m.put("endDate",   c.getEndDate()   != null ? c.getEndDate().toString()   : null);
        m.put("typeName",  c.getIdType()    != null ? c.getIdType().getName()     : null);
        return m;
    }

    private Map<String, Object> storeToMap(CampaignStore cs) {
        var s = cs.getIdStore();
        Map<String, Object> m = new HashMap<>();
        m.put("id",        s.getId());
        m.put("name",      s.getName());
        m.put("address",   s.getAddress());
        m.put("chainName", s.getIdChain() != null ? s.getIdChain().getName() : null);
        m.put("locality",  s.getPostalCode() != null && s.getPostalCode().getIdLocality() != null
                ? s.getPostalCode().getIdLocality().getName() : null);
        return m;
    }

    private Map<String, Object> shiftToMap(Shift s) {
        Map<String, Object> m = new HashMap<>();
        m.put("shiftId",          s.getId());
        m.put("campaignId",       s.getIdCampaign().getId());
        m.put("storeId",          s.getIdStore().getId());
        m.put("day",              s.getShiftDay() != null ? s.getShiftDay().toString() : null);
        m.put("startTime",        s.getStartTime() != null ? s.getStartTime().toString() : null);
        m.put("endTime",          s.getEndTime()   != null ? s.getEndTime().toString()   : null);
        m.put("volunteersNeeded", s.getVolunteersNeeded());
        m.put("observations",     s.getObservations() != null ? s.getObservations() : "");
        return m;
    }

    private Map<String, Object> volunteerShiftToMap(VolunteerShift vs) {
        Map<String, Object> m = new HashMap<>();
        m.put("volunteerId",    vs.getIdVolunteer().getId());
        m.put("volunteerName",  vs.getIdVolunteer().getName());
        m.put("phone",          vs.getIdVolunteer().getPhone());
        m.put("shiftDay",       vs.getId().getShiftDay() != null ? vs.getId().getShiftDay().toString() : null);
        m.put("startTime",      vs.getId().getStartTime() != null ? vs.getId().getStartTime().toString() : null);
        m.put("endTime",        vs.getEndTime() != null ? vs.getEndTime().toString() : null);
        m.put("attendance",     vs.getAttendance());
        return m;
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
