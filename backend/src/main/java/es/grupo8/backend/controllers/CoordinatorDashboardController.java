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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CaptainRequestRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.PartnerEntityRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dao.VolunteerRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;
import es.grupo8.backend.security.CoordinatorGuard;

@RestController
@RequestMapping("/api/coordinator")
public class CoordinatorDashboardController {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private CoordinatorGuard          coordinatorGuard;
    @Autowired private CoordinatorRepository     coordinatorRepository;
    @Autowired private CaptainRepository         captainRepository;
    @Autowired private CaptainRequestRepository  captainRequestRepository;
    @Autowired private CampaignStoreRepository   campaignStoreRepository;
    @Autowired private UserRepository            userRepository;
    @Autowired private VolunteerRepository       volunteerRepository;
    @Autowired private VolunteerShiftRepository  volunteerShiftRepository;
    @Autowired private PartnerEntityRepository   partnerEntityRepository;

    // ── GET /api/coordinator/my-campaigns ────────────────────────────────────

    @GetMapping("/my-campaigns")
    public ResponseEntity<?> getMyCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        Integer userId = coordinatorGuard.extractUserId(authHeader);
        List<Campaign> campaigns = coordinatorRepository.findCampaignsByUserId(userId);

        List<Map<String, Object>> result = campaigns.stream()
                .map(this::campaignToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/coordinator/my-stores?campaignId=X ──────────────────────────

    @GetMapping("/my-stores")
    public ResponseEntity<?> getMyStores(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "campaignId es obligatorio"));
        }

        List<CampaignStore> campaignStores = campaignStoreRepository.findByIdCampaign_Id(campaignId);

        List<Map<String, Object>> result = campaignStores.stream()
                .map(cs -> storeToMap(cs))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/coordinator/volunteers?campaignId=X ─────────────────────────

    @GetMapping("/volunteers")
    public ResponseEntity<?> getVolunteers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        List<Volunteer> volunteers = volunteerRepository.findAllByOrderByNameAsc();

        List<Map<String, Object>> result = volunteers.stream()
                .map(this::volunteerToMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── POST /api/coordinator/volunteers ─────────────────────────────────────

    @PostMapping("/volunteers")
    public ResponseEntity<?> createVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        Integer userId = coordinatorGuard.extractUserId(authHeader);

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        String name = trimToNull(request.get("name"));
        if (name == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
        }

        Volunteer v = new Volunteer();
        v.setName(name);
        v.setPhone(trimToNull(request.get("phone")));
        v.setEmail(trimToNull(request.get("email")));
        v.setAddress(trimToNull(request.get("address")));

        Integer partnerEntityId = parseInteger(request.get("partnerEntityId"));
        if (partnerEntityId != null) {
            PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId).orElse(null);
            if (pe == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Entidad colaboradora no encontrada con id=" + partnerEntityId));
            }
            v.setIdPartnerEntity(pe);
        }

        Volunteer saved = volunteerRepository.save(v);

        auditLog.info("ACTION=CREATE_VOLUNTEER userId={} timestamp={} volunteerId={}",
                userId, Instant.now(), saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(volunteerToMap(saved));
    }

    // ── PUT /api/coordinator/volunteers/{id} ──────────────────────────────────

    @PutMapping("/volunteers/{id}")
    public ResponseEntity<?> updateVolunteer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        Integer userId = coordinatorGuard.extractUserId(authHeader);

        Volunteer v = volunteerRepository.findById(id).orElse(null);
        if (v == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Voluntario no encontrado"));
        }

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        String name = trimToNull(request.get("name"));
        if (name == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El nombre es obligatorio"));
        }

        v.setName(name);
        v.setPhone(trimToNull(request.get("phone")));
        v.setEmail(trimToNull(request.get("email")));
        v.setAddress(trimToNull(request.get("address")));

        // partnerEntityId: null clears the entity, a valid id sets it, absent key keeps current
        if (request.containsKey("partnerEntityId")) {
            Integer partnerEntityId = parseInteger(request.get("partnerEntityId"));
            if (partnerEntityId == null) {
                v.setIdPartnerEntity(null);
            } else {
                PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId).orElse(null);
                if (pe == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Entidad colaboradora no encontrada con id=" + partnerEntityId));
                }
                v.setIdPartnerEntity(pe);
            }
        }

        Volunteer saved = volunteerRepository.save(v);

        auditLog.info("ACTION=UPDATE_VOLUNTEER userId={} timestamp={} volunteerId={}",
                userId, Instant.now(), saved.getId());

        return ResponseEntity.ok(volunteerToMap(saved));
    }

    // ── POST /api/coordinator/volunteer-shifts ────────────────────────────────

    @PostMapping("/volunteer-shifts")
    public ResponseEntity<?> assignVolunteerShift(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        Integer userId = coordinatorGuard.extractUserId(authHeader);

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        Integer volunteerId = parseInteger(request.get("volunteerId"));
        Integer campaignId  = parseInteger(request.get("campaignId"));
        Integer storeId     = parseInteger(request.get("storeId"));
        String  shiftDay    = trimToNull(request.get("shiftDay"));
        String  startTime   = trimToNull(request.get("startTime"));
        String  endTime     = trimToNull(request.get("endTime"));

        if (volunteerId == null || campaignId == null || storeId == null
                || shiftDay == null || startTime == null || endTime == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Campos obligatorios: volunteerId, campaignId, storeId, shiftDay, startTime, endTime"));
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId).orElse(null);
        if (volunteer == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Voluntario no encontrado"));
        }

        if (!campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La tienda no está asociada a esta campaña"));
        }

        CampaignStore cs = campaignStoreRepository
                .findByIdCampaign_Id(campaignId)
                .stream()
                .filter(x -> x.getIdStore().getId().equals(storeId))
                .findFirst()
                .orElse(null);

        if (cs == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Relación campaña-tienda no encontrada"));
        }

        java.time.LocalDate day;
        java.time.LocalTime start;
        java.time.LocalTime end;
        try {
            day   = java.time.LocalDate.parse(shiftDay);
            start = java.time.LocalTime.parse(startTime);
            end   = java.time.LocalTime.parse(endTime);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Formato de fecha u hora inválido. Use YYYY-MM-DD y HH:mm"));
        }

        if (!start.isBefore(end)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La hora de inicio debe ser anterior a la hora de fin"));
        }

        VolunteerShiftId vsId = new VolunteerShiftId();
        vsId.setIdVolunteer(volunteerId);
        vsId.setIdCampaign(campaignId);
        vsId.setIdStore(storeId);
        vsId.setShiftDay(day);
        vsId.setStartTime(start);

        VolunteerShift vs = new VolunteerShift();
        vs.setId(vsId);
        vs.setIdVolunteer(volunteer);
        vs.setCampaignStores(cs);
        vs.setEndTime(end);
        vs.setAttendance(false);

        volunteerShiftRepository.save(vs);

        auditLog.info("ACTION=ASSIGN_VOLUNTEER_SHIFT userId={} timestamp={} volunteerId={} campaignId={} storeId={} day={} start={}",
                userId, Instant.now(), volunteerId, campaignId, storeId, day, start);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Voluntario asignado al turno correctamente"));
    }

    // ── GET /api/coordinator/captains?campaignId=X&storeId=Y ─────────────────

    @GetMapping("/captains")
    public ResponseEntity<?> getCaptains(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId,
            @RequestParam(value = "storeId", required = false) Integer storeId) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "campaignId es obligatorio"));
        }

        List<Captain> captains = captainRepository.findByIdIdCampaign(campaignId);

        List<Map<String, Object>> result = captains.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", c.getIdUser().getIdUser());
            m.put("name",   c.getIdUser().getName());
            m.put("email",  c.getIdUser().getEmail());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── POST /api/coordinator/captains/register ───────────────────────────────

    @PostMapping("/captains/register")
    public ResponseEntity<?> registerCaptain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> request) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        Integer coordinatorUserId = coordinatorGuard.extractUserId(authHeader);

        if (request == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El cuerpo de la petición es obligatorio"));
        }

        String  name       = trimToNull(request.get("name"));
        String  email      = trimToNull(request.get("email"));
        String  password   = trimToNull(request.get("password"));
        Integer campaignId = parseInteger(request.get("campaignId"));

        if (name == null || email == null || password == null || campaignId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Campos obligatorios: name, email, password, campaignId"));
        }

        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La contraseña debe tener al menos 6 caracteres"));
        }

        String normalizedEmail = email.toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe un usuario registrado con ese email"));
        }

        if (captainRequestRepository.existsByEmailAndStatus(normalizedEmail, "PENDIENTE")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ya existe una solicitud pendiente para ese email"));
        }

        UserEntity coordinator = new UserEntity();
        coordinator.setIdUser(coordinatorUserId);

        Campaign campaign = new Campaign();
        campaign.setId(campaignId);

        CaptainRequest req = new CaptainRequest();
        req.setName(name);
        req.setEmail(normalizedEmail);
        req.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        req.setIdCampaign(campaign);
        req.setIdCoordinator(coordinator);
        req.setStatus("PENDIENTE");

        CaptainRequest saved = captainRequestRepository.save(req);

        auditLog.info("ACTION=REQUEST_CAPTAIN coordinatorUserId={} timestamp={} campaignId={} requestId={}",
                coordinatorUserId, Instant.now(), campaignId, saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message",   "Solicitud enviada. El administrador deberá aprobarla.",
                "requestId", saved.getId()
        ));
    }

    // ── GET /api/coordinator/partner-entities ─────────────────────────────────

    @GetMapping("/partner-entities")
    public ResponseEntity<?> getPartnerEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        List<Map<String, Object>> result = partnerEntityRepository.findAll().stream()
                .map(pe -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",    pe.getId());
                    m.put("name",  pe.getName());
                    m.put("phone", pe.getPhone());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── GET /api/coordinator/campaign-entities?campaignId=X ──────────────────

    @GetMapping("/campaign-entities")
    public ResponseEntity<?> getCampaignEntities(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "campaignId", required = false) Integer campaignId) {

        if (!coordinatorGuard.isCoordinator(authHeader)) {
            return forbidden();
        }

        if (campaignId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "campaignId es obligatorio"));
        }

        List<PartnerEntity> entities = volunteerShiftRepository.findEntitiesWithVolunteersInCampaign(campaignId);

        List<Map<String, Object>> result = entities.stream().map(pe -> {
            Long count = volunteerShiftRepository.countVolunteersInCampaignByEntity(campaignId, pe.getId());
            Map<String, Object> m = new HashMap<>();
            m.put("id",             pe.getId());
            m.put("name",           pe.getName());
            m.put("phone",          pe.getPhone());
            m.put("volunteerCount", count != null ? count : 0L);
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

    private Map<String, Object> volunteerToMap(Volunteer v) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",                v.getId());
        m.put("name",              v.getName());
        m.put("phone",             v.getPhone());
        m.put("email",             v.getEmail());
        m.put("address",           v.getAddress());
        m.put("partnerEntityId",   v.getIdPartnerEntity() != null ? v.getIdPartnerEntity().getId()   : null);
        m.put("partnerEntityName", v.getIdPartnerEntity() != null ? v.getIdPartnerEntity().getName() : null);
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
