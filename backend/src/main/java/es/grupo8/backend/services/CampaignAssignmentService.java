/**
 * Servicio de asignación de coordinadores y capitanes a campañas.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 75%
 * - IA Generativa: 25%
 */
package es.grupo8.backend.services;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.AssignmentResultDTO;
import es.grupo8.backend.dto.CampaignAssignmentsDTO;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;
import es.grupo8.backend.entity.UserEntity;
import es.grupo8.backend.mapper.CampaignAssignmentsMapper;
import es.grupo8.backend.mapper.CampaignMapper;
import es.grupo8.backend.mapper.UserMapper;
import lombok.AllArgsConstructor;

/**
 * Service for admin management of coordinator and captain assignments to campaigns (RF-14).
 */
@Service
@AllArgsConstructor
public class CampaignAssignmentService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final CaptainRepository captainRepository;

    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;
    private final CampaignAssignmentsMapper campaignAssignmentsMapper;

    /**
     * Returns all campaigns as a typed list for the admin assignment view.
     *
     * @param adminUserId admin user identifier (for audit)
     * @return list of campaign DTOs
     */
    @Transactional(readOnly = true)
    public List<CampaignDTO> getCampaigns(Integer adminUserId) {
        List<CampaignDTO> result = campaignMapper.toDTOList(campaignRepository.findAll());
        auditLog.info("ACTION=LIST_CAMPAIGNS adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), null, null);
        return result;
    }

    /**
     * Returns the coordinator and captain assignments for a campaign.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @return typed DTO with campaign info and coordinator/captain lists
     * @throws NoSuchElementException if campaign not found
     */
    @Transactional(readOnly = true)
    public CampaignAssignmentsDTO getCampaignAssignments(Integer adminUserId, Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<UserDTO> coordinators = userMapper.toDTOList(
                coordinatorRepository.findUsersByCampaignId(campaignId));
        List<UserDTO> captains = userMapper.toDTOList(
                captainRepository.findUsersByCampaignId(campaignId));

        auditLog.info("ACTION=GET_CAMPAIGN_ASSIGNMENTS adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, null);

        return campaignAssignmentsMapper.toDTO(campaign, coordinators, captains);
    }

    /**
     * Returns users eligible to be assigned to a campaign in the given role.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @param role        required role filter (COORDINATOR or CAPTAIN)
     * @return list of available user DTOs
     * @throws NoSuchElementException   if campaign not found
     * @throws IllegalArgumentException if role is invalid
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAvailableUsers(Integer adminUserId, Integer campaignId, String role) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (role == null || (!"COORDINATOR".equalsIgnoreCase(role) && !"CAPTAIN".equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Invalid role. Use COORDINATOR or CAPTAIN");
        }

        List<UserDTO> result = "COORDINATOR".equalsIgnoreCase(role)
                ? userMapper.toDTOList(userRepository.findAvailableCoordinators(campaignId))
                : userMapper.toDTOList(userRepository.findAvailableCaptains(campaignId));

        auditLog.info("ACTION=LIST_AVAILABLE_USERS_FOR_CAMPAIGN adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, null);
        return result;
    }

    /**
     * Assigns a coordinator to a campaign.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @param userId      coordinator user identifier
     * @return typed assignment result DTO
     * @throws NoSuchElementException   if campaign or user not found
     * @throws IllegalArgumentException if user is not a coordinator
     * @throws IllegalStateException    if user is already assigned
     */
    public AssignmentResultDTO assignCoordinator(Integer adminUserId, Integer campaignId, Integer userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!userRepository.isCoordinator(userId)) {
            throw new IllegalArgumentException("User does not have the coordinator role");
        }
        if (coordinatorRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new IllegalStateException("User is already assigned as coordinator in this campaign");
        }

        CoordinatorId coordinatorId = new CoordinatorId();
        coordinatorId.setIdUser(userId);
        coordinatorId.setIdCampaign(campaignId);

        Coordinator coordinator = new Coordinator();
        coordinator.setId(coordinatorId);
        coordinator.setIdUser(user);
        coordinator.setIdCampaign(campaign);
        coordinatorRepository.save(coordinator);

        auditLog.info("ACTION=ASSIGN_COORDINATOR adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, userId);

        return new AssignmentResultDTO("Coordinator assigned successfully", campaignId, userId, user.getName());
    }

    /**
     * Removes a coordinator from a campaign.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @param userId      coordinator user identifier
     * @throws NoSuchElementException if campaign or assignment not found
     */
    @Transactional
    public void unassignCoordinator(Integer adminUserId, Integer campaignId, Integer userId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new NoSuchElementException("Assignment not found");
        }
        coordinatorRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);
        auditLog.info("ACTION=UNASSIGN_COORDINATOR adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, userId);
    }

    /**
     * Assigns a captain to a campaign.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @param userId      captain user identifier
     * @return typed assignment result DTO
     * @throws NoSuchElementException   if campaign or user not found
     * @throws IllegalArgumentException if user is not a captain
     * @throws IllegalStateException    if user is already assigned
     */
    public AssignmentResultDTO assignCaptain(Integer adminUserId, Integer campaignId, Integer userId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!userRepository.isCaptain(userId)) {
            throw new IllegalArgumentException("User does not have the captain role");
        }
        if (captainRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new IllegalStateException("User is already assigned as captain in this campaign");
        }

        CaptainId captainId = new CaptainId();
        captainId.setIdUser(userId);
        captainId.setIdCampaign(campaignId);

        Captain captain = new Captain();
        captain.setId(captainId);
        captain.setIdUser(user);
        captain.setIdCampaign(campaign);
        captainRepository.save(captain);

        auditLog.info("ACTION=ASSIGN_CAPTAIN adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, userId);

        return new AssignmentResultDTO("Captain assigned successfully", campaignId, userId, user.getName());
    }

    /**
     * Removes a captain from a campaign.
     *
     * @param adminUserId admin user identifier (for audit)
     * @param campaignId  target campaign identifier
     * @param userId      captain user identifier
     * @throws NoSuchElementException if campaign or assignment not found
     */
    @Transactional
    public void unassignCaptain(Integer adminUserId, Integer campaignId, Integer userId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (!captainRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new NoSuchElementException("Assignment not found");
        }
        captainRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);
        auditLog.info("ACTION=UNASSIGN_CAPTAIN adminUserId={} timestamp={} campaignId={} affectedUserId={}",
                adminUserId, Instant.now(), campaignId, userId);
    }

}
