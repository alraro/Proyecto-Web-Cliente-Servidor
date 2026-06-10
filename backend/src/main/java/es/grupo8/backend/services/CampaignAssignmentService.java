package es.grupo8.backend.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dao.UserRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.CaptainId;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.CoordinatorId;
import es.grupo8.backend.entity.UserEntity;
import lombok.AllArgsConstructor;

// RF-14: admin management of coordinator and captain assignments to campaigns.
@Service
@AllArgsConstructor
public class CampaignAssignmentService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CoordinatorRepository coordinatorRepository;
    private final CaptainRepository captainRepository;

    public List<Map<String, Object>> getCampaigns(Integer adminUserId) {
        List<Map<String, Object>> result = campaignRepository.findAll().stream()
                .map(this::toCampaignMap)
                .collect(Collectors.toList());
        return result;
    }

    public Map<String, Object> getCampaignAssignments(Integer adminUserId, Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<Map<String, Object>> coordinators = usersFromCoordinators(
                coordinatorRepository.findByIdIdCampaign(campaignId));
        List<Map<String, Object>> captains = usersFromCaptains(
                captainRepository.findByIdIdCampaign(campaignId));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("campaignId", campaign.getId());
        response.put("campaignName", campaign.getName());
        response.put("coordinators", coordinators);
        response.put("captains", captains);
        return response;
    }

    public List<Map<String, Object>> getAvailableUsers(Integer adminUserId, Integer campaignId, String role) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (role == null || (!"COORDINATOR".equalsIgnoreCase(role) && !"CAPTAIN".equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Invalid role. Use COORDINATOR or CAPTAIN");
        }

        List<Map<String, Object>> result = "COORDINATOR".equalsIgnoreCase(role)
                ? getAvailableCoordinatorUsers(campaignId)
                : getAvailableCaptainUsers(campaignId);

        return result;
    }

    public Map<String, Object> assignCoordinator(Integer adminUserId, Integer campaignId, Integer userId) {
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Coordinator assigned successfully");
        response.put("campaignId", campaignId);
        response.put("userId", userId);
        response.put("userName", user.getName());
        return response;
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

    public Map<String, Object> assignCaptain(Integer adminUserId, Integer campaignId, Integer userId) {
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Captain assigned successfully");
        response.put("campaignId", campaignId);
        response.put("userId", userId);
        response.put("userName", user.getName());
        return response;
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

    private Map<String, Object> toCampaignMap(Campaign campaign) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", campaign.getId());
        map.put("name", campaign.getName());
        map.put("startDate", campaign.getStartDate() == null ? null : campaign.getStartDate().toString());
        map.put("endDate", campaign.getEndDate() == null ? null : campaign.getEndDate().toString());
        map.put("type", campaign.getIdType() == null ? null : campaign.getIdType().getName());
        return map;
    }

    private List<Map<String, Object>> getAvailableCoordinatorUsers(Integer campaignId) {
        Set<Integer> assignedIds = coordinatorRepository.findByIdIdCampaign(campaignId).stream()
                .filter(c -> c.getId() != null && c.getId().getIdUser() != null)
                .map(c -> c.getId().getIdUser())
                .collect(Collectors.toSet());

        return userRepository.findAllCoordinators().stream()
                .filter(user -> user != null && user.getIdUser() != null)
                .filter(user -> !assignedIds.contains(user.getIdUser()))
                .map(this::toUserMap)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAvailableCaptainUsers(Integer campaignId) {
        Set<Integer> assignedIds = captainRepository.findByIdIdCampaign(campaignId).stream()
                .filter(c -> c.getId() != null && c.getId().getIdUser() != null)
                .map(c -> c.getId().getIdUser())
                .collect(Collectors.toSet());

        return userRepository.findAllCaptains().stream()
                .filter(user -> user != null && user.getIdUser() != null)
                .filter(user -> !assignedIds.contains(user.getIdUser()))
                .map(this::toUserMap)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> usersFromCoordinators(List<Coordinator> assignments) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Coordinator assignment : assignments) {
            if (assignment == null || assignment.getId() == null || assignment.getId().getIdUser() == null) continue;
            Integer userId = assignment.getId().getIdUser();
            userRepository.findById(userId).ifPresent(user -> result.add(toUserMap(user)));
        }
        return result;
    }

    private List<Map<String, Object>> usersFromCaptains(List<Captain> assignments) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Captain assignment : assignments) {
            if (assignment == null || assignment.getId() == null || assignment.getId().getIdUser() == null) continue;
            Integer userId = assignment.getId().getIdUser();
            userRepository.findById(userId).ifPresent(user -> result.add(toUserMap(user)));
        }
        return result;
    }

    private Map<String, Object> toUserMap(UserEntity user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userId", user.getIdUser());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        return map;
    }
}
