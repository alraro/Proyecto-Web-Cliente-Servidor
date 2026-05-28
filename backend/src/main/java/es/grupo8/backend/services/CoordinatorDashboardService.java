package es.grupo8.backend.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

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
import es.grupo8.backend.entity.CaptainRequest;
import es.grupo8.backend.entity.PartnerEntity;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.entity.Volunteer;
import es.grupo8.backend.entity.VolunteerShift;
import es.grupo8.backend.entity.VolunteerShiftId;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CoordinatorDashboardService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final CoordinatorRepository coordinatorRepository;
    private final CaptainRepository captainRepository;
    private final CaptainRequestRepository captainRequestRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final UserRepository userRepository;
    private final VolunteerRepository volunteerRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;
    private final PartnerEntityRepository partnerEntityRepository;

    public List<Map<String, Object>> getMyCampaigns(Integer userId) {
        return coordinatorRepository.findCampaignsByUserId(userId).stream()
                .map(this::campaignToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyStores(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return campaignStoreRepository.findByIdCampaign_Id(campaignId).stream()
                .map(this::storeToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getVolunteers() {
        return volunteerRepository.findAllByOrderByNameAsc().stream()
                .map(this::volunteerToMap)
                .collect(Collectors.toList());
    }

    public Map<String, Object> createVolunteer(Integer coordinatorId, String name, String phone,
            String email, String address, Integer partnerEntityId) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        Volunteer v = new Volunteer();
        v.setName(name);
        v.setPhone(phone);
        v.setEmail(email);
        v.setAddress(address);

        if (partnerEntityId != null) {
            PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Entidad colaboradora no encontrada con id=" + partnerEntityId));
            v.setIdPartnerEntity(pe);
        }

        Volunteer saved = volunteerRepository.save(v);
        auditLog.info("ACTION=CREATE_VOLUNTEER userId={} timestamp={} volunteerId={}",
                coordinatorId, Instant.now(), saved.getId());
        return volunteerToMap(saved);
    }

    public Map<String, Object> updateVolunteer(Integer coordinatorId, Integer volunteerId,
            String name, String phone, String email, String address,
            boolean partnerEntityKeyPresent, Integer partnerEntityId) {

        Volunteer v = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new NoSuchElementException("Voluntario no encontrado"));
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        v.setName(name);
        v.setPhone(phone);
        v.setEmail(email);
        v.setAddress(address);

        if (partnerEntityKeyPresent) {
            if (partnerEntityId == null) {
                v.setIdPartnerEntity(null);
            } else {
                PartnerEntity pe = partnerEntityRepository.findById(partnerEntityId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Entidad colaboradora no encontrada con id=" + partnerEntityId));
                v.setIdPartnerEntity(pe);
            }
        }

        Volunteer saved = volunteerRepository.save(v);
        auditLog.info("ACTION=UPDATE_VOLUNTEER userId={} timestamp={} volunteerId={}",
                coordinatorId, Instant.now(), saved.getId());
        return volunteerToMap(saved);
    }

    public void assignVolunteerShift(Integer coordinatorId, Integer volunteerId, Integer campaignId,
            Integer storeId, String shiftDay, String startTime, String endTime) {

        if (volunteerId == null || campaignId == null || storeId == null
                || shiftDay == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Campos obligatorios: volunteerId, campaignId, storeId, shiftDay, startTime, endTime");
        }

        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new IllegalArgumentException("Voluntario no encontrado"));

        if (!campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
            throw new IllegalArgumentException("La tienda no está asociada a esta campaña");
        }

        CampaignStore cs = campaignStoreRepository.findByIdCampaign_Id(campaignId).stream()
                .filter(x -> x.getIdStore().getId().equals(storeId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Relación campaña-tienda no encontrada"));

        LocalDate day;
        LocalTime start;
        LocalTime end;
        try {
            day   = LocalDate.parse(shiftDay);
            start = LocalTime.parse(startTime);
            end   = LocalTime.parse(endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de fecha u hora inválido. Use YYYY-MM-DD y HH:mm");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
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
                coordinatorId, Instant.now(), volunteerId, campaignId, storeId, day, start);
    }

    public List<Map<String, Object>> getCaptains(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return captainRepository.findByIdIdCampaign(campaignId).stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userId", c.getIdUser().getIdUser());
                    m.put("name",   c.getIdUser().getName());
                    m.put("email",  c.getIdUser().getEmail());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> registerCaptain(Integer coordinatorId, String name, String email,
            String password, Integer campaignId) {

        if (name == null || email == null || password == null || campaignId == null) {
            throw new IllegalArgumentException("Campos obligatorios: name, email, password, campaignId");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        String normalizedEmail = email.toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Ya existe un usuario registrado con ese email");
        }
        if (captainRequestRepository.existsByEmailAndStatus(normalizedEmail, "PENDIENTE")) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para ese email");
        }

        UserEntity coordinator = new UserEntity();
        coordinator.setIdUser(coordinatorId);

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
                coordinatorId, Instant.now(), campaignId, saved.getId());

        return Map.of("message", "Solicitud enviada. El administrador deberá aprobarla.",
                      "requestId", saved.getId());
    }

    public List<Map<String, Object>> getPartnerEntities() {
        return partnerEntityRepository.findAll().stream()
                .map(pe -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",    pe.getId());
                    m.put("name",  pe.getName());
                    m.put("phone", pe.getPhone());
                    return m;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getCampaignEntities(Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return volunteerShiftRepository.findEntitiesWithVolunteersInCampaign(campaignId).stream()
                .map(pe -> {
                    Long count = volunteerShiftRepository.countVolunteersInCampaignByEntity(campaignId, pe.getId());
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",             pe.getId());
                    m.put("name",           pe.getName());
                    m.put("phone",          pe.getPhone());
                    m.put("volunteerCount", count != null ? count : 0L);
                    return m;
                })
                .collect(Collectors.toList());
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
}
