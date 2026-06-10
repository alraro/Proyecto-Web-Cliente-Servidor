/**
 * Servicio de lógica de negocio de campañas.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CampaignTypeRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CampaignRequestDto;
import es.grupo8.backend.dto.CampaignTypeResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignType;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.CampaignTypeMapper;

/**
 * Service for campaign management operations.
 * Throws IllegalArgumentException (400), NoSuchElementException (404), IllegalStateException (409).
 */
@Service
public class CampaignService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Autowired private CampaignRepository campaignRepository;
    @Autowired private CampaignTypeRepository campaignTypeRepository;
    @Autowired private CoordinatorRepository coordinatorRepository;
    @Autowired private CaptainRepository captainRepository;
    @Autowired private CampaignStoreRepository campaignStoreRepository;
    @Autowired private CampaignMapper campaignMapper;
    @Autowired private CampaignTypeMapper campaignTypeMapper;

    /** Returns all campaign types. */
    public List<CampaignTypeResponseDto> getCampaignTypes() {
        return campaignTypeMapper.toDTOList(campaignTypeRepository.findAll());
    }

    /**
     * Returns a paginated and optionally filtered list of campaigns.
     *
     * @param status ACTIVE, PAST, FUTURE or null for all
     * @param pageable pagination and sorting settings
     */
    public Page<CampaignDTO> getCampaigns(String status, Pageable pageable) {
        validateStatus(status);
        LocalDate today = LocalDate.now();
        Page<Campaign> page = status == null
                ? campaignRepository.findAll(pageable)
                : switch (status.toUpperCase()) {
                    case "PAST"   -> campaignRepository.findByEndDateBefore(today, pageable);
                    case "FUTURE" -> campaignRepository.findByStartDateAfter(today, pageable);
                    default       -> campaignRepository
                            .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today, pageable);
                };
        return page.map(campaignMapper::toDTO);
    }

    /**
     * Returns [totalActive, totalPast, totalFuture] counts for the summary section.
     */
    public long[] getCampaignCounts() {
        LocalDate today = LocalDate.now();
        return new long[]{
            campaignRepository.countByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today),
            campaignRepository.countByEndDateBefore(today),
            campaignRepository.countByStartDateAfter(today)
        };
    }

    /** Returns the campaign with the given id, or empty if not found. */
    public Optional<CampaignDTO> getCampaignById(Integer id) {
        return campaignRepository.findById(id).map(campaignMapper::toDTO);
    }

    /**
     * Creates a new campaign.
     *
     * @param request campaign data
     * @return the created campaign DTO
     */
    public CampaignDTO createCampaign(Integer adminUserId, CampaignRequestDto request) {
        validate(request);
        CampaignType type = findType(request.getTypeId());
        if (campaignRepository.existsByName(request.getName().trim())) {
            throw new IllegalStateException("A campaign with this name already exists");
        }
        Campaign campaign = new Campaign();
        campaign.setName(request.getName().trim());
        campaign.setIdType(type);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        CampaignDTO result = campaignMapper.toDTO(campaignRepository.save(campaign));
        auditLog.info("ACTION=CREATE_CAMPAIGN adminUserId={} timestamp={} campaignId={} campaignName={}",
                adminUserId, Instant.now(), result.getId(), result.getName());
        return result;
    }

    /**
     * Updates an existing campaign.
     *
     * @param id      campaign id
     * @param request updated data
     * @return the updated campaign DTO
     */
    public CampaignDTO updateCampaign(Integer adminUserId, Integer id, CampaignRequestDto request) {
        validate(request);
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        CampaignType type = findType(request.getTypeId());
        if (campaignRepository.existsByNameAndIdNot(request.getName().trim(), id)) {
            throw new IllegalStateException("A campaign with this name already exists");
        }
        campaign.setName(request.getName().trim());
        campaign.setIdType(type);
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        CampaignDTO result = campaignMapper.toDTO(campaignRepository.save(campaign));
        auditLog.info("ACTION=UPDATE_CAMPAIGN adminUserId={} timestamp={} campaignId={} campaignName={}",
                adminUserId, Instant.now(), result.getId(), result.getName());
        return result;
    }

    /**
     * Deletes a campaign and all its related assignments.
     *
     * @param id campaign id
     * @return the name of the deleted campaign (for audit logging)
     */
    @Transactional
    public void deleteCampaign(Integer adminUserId, Integer id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        String name = campaign.getName();
        coordinatorRepository.deleteAllByIdIdCampaign(id);
        captainRepository.deleteAllByIdIdCampaign(id);
        campaignStoreRepository.deleteByCampaignId(id);
        campaignRepository.deleteById(id);
        auditLog.info("ACTION=DELETE_CAMPAIGN adminUserId={} timestamp={} campaignId={} campaignName={}",
                adminUserId, Instant.now(), id, name);
    }

    private void validateStatus(String status) {
        if (status != null
                && !status.equalsIgnoreCase("ACTIVE")
                && !status.equalsIgnoreCase("PAST")
                && !status.equalsIgnoreCase("FUTURE")) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE, PAST or FUTURE");
        }
    }

    private void validate(CampaignRequestDto r) {
        if (r == null || r.getName() == null || r.getName().trim().isEmpty())
            throw new IllegalArgumentException("Campaign name is required");
        if (r.getTypeId() == null)
            throw new IllegalArgumentException("Campaign type is required");
        if (r.getStartDate() == null)
            throw new IllegalArgumentException("Start date is required");
        if (r.getEndDate() == null)
            throw new IllegalArgumentException("End date is required");
        if (!r.getEndDate().isAfter(r.getStartDate()))
            throw new IllegalArgumentException("End date must be after start date");
    }

    private CampaignType findType(Integer typeId) {
        return campaignTypeRepository.findById(typeId)
                .orElseThrow(() -> new NoSuchElementException("Campaign type not found"));
    }
}
