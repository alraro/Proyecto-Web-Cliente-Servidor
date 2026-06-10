/**
 * Servicio del panel del capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.IncidentRepository;
import es.grupo8.backend.dao.ShiftRepository;
import es.grupo8.backend.dao.VolunteerShiftRepository;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.IncidentDTO;
import es.grupo8.backend.dto.ShiftResponseDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.dto.VolunteerShiftDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Incident;
import es.grupo8.backend.entity.Shift;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.IncidentMapper;
import es.grupo8.backend.mapper.ShiftMapper;
import es.grupo8.backend.mapper.StoreMapper;
import es.grupo8.backend.mapper.VolunteerShiftMapper;
import lombok.AllArgsConstructor;

/**
 * Service for captain dashboard operations: campaigns, stores, shifts, volunteer shifts and incidents.
 */
@Service
@AllArgsConstructor
public class CaptainDashboardService {

    private final CaptainRepository captainRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final ShiftRepository shiftRepository;
    private final VolunteerShiftRepository volunteerShiftRepository;
    private final IncidentRepository incidentRepository;

    private final CampaignMapper campaignMapper;
    private final StoreMapper storeMapper;
    private final ShiftMapper shiftMapper;
    private final VolunteerShiftMapper volunteerShiftMapper;
    private final IncidentMapper incidentMapper;

    /**
     * Returns the campaigns assigned to the given captain.
     *
     * @param userId captain user identifier
     * @return list of campaign DTOs
     */
    @Transactional(readOnly = true)
    public List<CampaignDTO> getMyCampaigns(Integer userId) {
        return campaignMapper.toDTOList(captainRepository.findCampaignsByUserId(userId));
    }

    /**
     * Returns the stores assigned to the captain for a given campaign.
     *
     * @param userId     captain user identifier
     * @param campaignId required campaign filter
     * @return list of store DTOs
     * @throws IllegalArgumentException if campaignId is null
     */
    @Transactional(readOnly = true)
    public List<StoreResponseDto> getMyStores(Integer userId, Integer campaignId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return storeMapper.toDTOList(campaignStoreRepository.findStoresByCampaignId(campaignId));
    }

    /**
     * Returns shifts for the campaign, optionally filtered by store.
     *
     * @param userId     captain user identifier
     * @param campaignId required campaign filter
     * @param storeId    optional store filter
     * @return list of shift DTOs
     * @throws IllegalArgumentException if campaignId is null
     */
    @Transactional(readOnly = true)
    public List<ShiftResponseDto> getShifts(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        List<Shift> shifts = (storeId != null)
                ? shiftRepository.findByCampaignAndStore(campaignId, storeId)
                : shiftRepository.findByIdCampaign(campaignId);
        return shiftMapper.toDTOList(shifts);
    }

    /**
     * Returns volunteer-shift assignments filtered by campaign and optionally by store.
     *
     * @param userId     captain user identifier
     * @param campaignId required campaign filter
     * @param storeId    optional store filter; when null, returns all shifts for the campaign
     * @return list of volunteer-shift DTOs
     * @throws IllegalArgumentException if campaignId is null
     */
    @Transactional(readOnly = true)
    public List<VolunteerShiftDTO> getVolunteerShifts(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null) {
            throw new IllegalArgumentException("campaignId es obligatorio");
        }
        return volunteerShiftMapper.toDTOList(
                volunteerShiftRepository.findByCampaignAndOptionalStore(campaignId, storeId));
    }

    /**
     * Creates an incident report for the given campaign store.
     *
     * @param userId      captain user identifier
     * @param campaignId  required campaign identifier
     * @param storeId     required store identifier
     * @param description required incident description
     * @return identifier of the created incident
     * @throws IllegalArgumentException if any required field is null
     */
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
        return saved.getId();
    }

    /**
     * Returns incidents for the given campaign store.
     *
     * @param userId     captain user identifier
     * @param campaignId required campaign filter
     * @param storeId    required store filter
     * @return list of incident DTOs
     * @throws IllegalArgumentException if campaignId or storeId is null
     */
    @Transactional(readOnly = true)
    public List<IncidentDTO> getIncidents(Integer userId, Integer campaignId, Integer storeId) {
        if (campaignId == null || storeId == null) {
            throw new IllegalArgumentException("campaignId y storeId son obligatorios");
        }
        return incidentMapper.toDTOList(
                incidentRepository.findByCampaignAndStore(campaignId, storeId));
    }
}
