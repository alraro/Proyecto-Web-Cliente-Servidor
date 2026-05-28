package es.grupo8.backend.services;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CaptainDashboardService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final CaptainRepository captainRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final ShiftRepository shiftRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;
    private final IncidentRepository incidentRepository;

    public List<Map<String, Object>> getMyCampaigns(Integer userId) {
        return captainRepository.findCampaignsByUserId(userId).stream()
                .map(this::campaignToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMyStores(Integer userId, Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return campaignStoreRepository.findByIdCampaign_Id(campaignId).stream()
                .map(this::storeToMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getShifts(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        List<Shift> shifts = (storeId != null)
                ? shiftRepository.findByCampaignAndStore(campaignId, storeId)
                : shiftRepository.findByIdCampaign(campaignId);
        return shifts.stream().map(this::shiftToMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getVolunteerShifts(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null || storeId == null) {
            throw new IllegalArgumentException("campaignId y storeId son obligatorios");
        }
        return volunteerShiftRepository.findAll().stream()
                .filter(vs -> vs.getId().getIdCampaign().equals(campaignId)
                           && vs.getId().getIdStore().equals(storeId))
                .map(this::volunteerShiftToMap)
                .collect(Collectors.toList());
    }

    public Integer createIncident(Integer userId, Integer campaignId, Integer storeId, String description) {
        if (campaignId == null || storeId == null || description == null) {
            throw new IllegalArgumentException("Campos obligatorios: campaignId, storeId, description");
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

        return saved.getId();
    }

    public List<Map<String, Object>> getIncidents(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null || storeId == null) {
            throw new IllegalArgumentException("campaignId y storeId son obligatorios");
        }
        return incidentRepository.findByCampaignAndStore(campaignId, storeId).stream()
                .map(i -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",           i.getId());
                    m.put("description",  i.getDescription());
                    m.put("createdAt",    i.getCreatedAt() != null ? i.getCreatedAt().toString() : null);
                    m.put("campaignName", i.getIdCampaign() != null ? i.getIdCampaign().getName() : null);
                    m.put("storeName",    i.getIdStore()    != null ? i.getIdStore().getName()    : null);
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

    private Map<String, Object> shiftToMap(Shift s) {
        Map<String, Object> m = new HashMap<>();
        m.put("shiftId",          s.getId());
        m.put("campaignId",       s.getIdCampaign().getId());
        m.put("storeId",          s.getIdStore().getId());
        m.put("day",              s.getShiftDay()  != null ? s.getShiftDay().toString()  : null);
        m.put("startTime",        s.getStartTime() != null ? s.getStartTime().toString() : null);
        m.put("endTime",          s.getEndTime()   != null ? s.getEndTime().toString()   : null);
        m.put("volunteersNeeded", s.getVolunteersNeeded());
        m.put("observations",     s.getObservations() != null ? s.getObservations() : "");
        return m;
    }

    private Map<String, Object> volunteerShiftToMap(VolunteerShift vs) {
        Map<String, Object> m = new HashMap<>();
        m.put("volunteerId",   vs.getIdVolunteer().getId());
        m.put("volunteerName", vs.getIdVolunteer().getName());
        m.put("phone",         vs.getIdVolunteer().getPhone());
        m.put("shiftDay",      vs.getId().getShiftDay()   != null ? vs.getId().getShiftDay().toString()   : null);
        m.put("startTime",     vs.getId().getStartTime()  != null ? vs.getId().getStartTime().toString()  : null);
        m.put("endTime",       vs.getEndTime()            != null ? vs.getEndTime().toString()             : null);
        m.put("attendance",    vs.getAttendance());
        return m;
    }
}
