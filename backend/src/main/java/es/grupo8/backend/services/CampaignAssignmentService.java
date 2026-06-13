/**
 * Servicio de asignación de coordinadores y capitanes a campañas.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 75%
 * - IA Generativa: 25%
 */
package es.grupo8.backend.services;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.dto.AssignmentResultDTO;
import es.grupo8.backend.dto.CampaignAssignmentsDTO;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.UserResponseDto;
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

@Service
@AllArgsConstructor
public class CampaignAssignmentService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final CaptainRepository captainRepository;

    private final CampaignMapper campaignMapper;
    private final UserMapper userMapper;
    private final CampaignAssignmentsMapper campaignAssignmentsMapper;

    @Transactional(readOnly = true)
    public List<CampaignDTO> getCampaigns(Integer adminUserId) {
        return campaignMapper.toDTOList(campaignRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CampaignAssignmentsDTO getCampaignAssignments(Integer adminUserId, Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<UserResponseDto> coordinators = userMapper.toDTOList(
                coordinatorRepository.findUsersByCampaignId(campaignId));
        List<UserResponseDto> captains = userMapper.toDTOList(
                captainRepository.findUsersByCampaignId(campaignId));

        return campaignAssignmentsMapper.toDTO(campaign, coordinators, captains);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAvailableUsers(Integer adminUserId, Integer campaignId, String role) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (role == null || (!"COORDINATOR".equalsIgnoreCase(role) && !"CAPTAIN".equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Invalid role. Use COORDINATOR or CAPTAIN");
        }

        return "COORDINATOR".equalsIgnoreCase(role)
                ? userMapper.toDTOList(userRepository.findAvailableCoordinators(campaignId))
                : userMapper.toDTOList(userRepository.findAvailableCaptains(campaignId));
    }

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

        AssignmentResultDTO result = new AssignmentResultDTO();
        result.setMessage("Coordinator assigned successfully");
        result.setCampaignId(campaignId);
        result.setUserId(userId);
        result.setUserName(user.getName());
        return result;
    }

    @Transactional
    public void unassignCoordinator(Integer adminUserId, Integer campaignId, Integer userId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new NoSuchElementException("Assignment not found");
        }
        coordinatorRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);
    }

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

        AssignmentResultDTO result = new AssignmentResultDTO();
        result.setMessage("Captain assigned successfully");
        result.setCampaignId(campaignId);
        result.setUserId(userId);
        result.setUserName(user.getName());
        return result;
    }

    @Transactional
    public void unassignCaptain(Integer adminUserId, Integer campaignId, Integer userId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (!captainRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
            throw new NoSuchElementException("Assignment not found");
        }
        captainRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);
    }

}
