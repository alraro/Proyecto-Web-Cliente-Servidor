package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignAssignmentController {

	// Centralized audit logger for admin actions over campaign assignments.

	private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

	@Autowired
	private AdminGuard adminGuard;

	@Autowired
	private CampaignRepository campaignRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CoordinatorRepository coordinatorRepository;

	@Autowired
	private CaptainRepository captainRepository;

	@GetMapping("/admin-list")
	public ResponseEntity<?> getCampaigns(
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		List<Map<String, Object>> campaigns = campaignRepository.findAll().stream()
				.map(this::toCampaignMap)
				.collect(Collectors.toList());

		logAudit("LIST_CAMPAIGNS", authHeader, null, null);
		return ResponseEntity.ok(campaigns);
	}

	@GetMapping("/{campaignId}/assignments")
	public ResponseEntity<?> getCampaignAssignments(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		List<Map<String, Object>> coordinators = usersFromCoordinators(
				coordinatorRepository.findByIdIdCampaign(campaignId));
		List<Map<String, Object>> captains = usersFromCaptains(
				captainRepository.findByIdIdCampaign(campaignId));

		// Build a stable JSON shape expected by JSP/HTML/React clients.
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("campaignId", campaign.getId());
		response.put("campaignName", campaign.getName());
		response.put("coordinators", coordinators);
		response.put("captains", captains);

		logAudit("GET_CAMPAIGN_ASSIGNMENTS", authHeader, campaignId, null);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{campaignId}/available-users")
	public ResponseEntity<?> getAvailableUsers(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@RequestParam(value = "role", required = false) String roleParam) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		String role = trimToNull(roleParam);
		if (role == null || (!"COORDINATOR".equalsIgnoreCase(role) && !"CAPTAIN".equalsIgnoreCase(role))) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "Invalid role. Use COORDINATOR or CAPTAIN"));
		}

		// Fetch role candidates and subtract users already assigned in this campaign.
		List<Map<String, Object>> response = "COORDINATOR".equalsIgnoreCase(role)
				? getAvailableCoordinatorUsers(campaignId)
				: getAvailableCaptainUsers(campaignId);

		logAudit("LIST_AVAILABLE_USERS_FOR_CAMPAIGN", authHeader, campaignId, null);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{campaignId}/coordinators")
	public ResponseEntity<?> assignCoordinator(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@RequestBody(required = false) Map<String, Object> request) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		// Validation chain: campaign -> userId -> user existence -> role -> duplicate assignment.
		Integer userId = extractUserId(request);
		if (userId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
		}

		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "User not found"));
		}

		if (!userRepository.isCoordinator(userId)) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "User does not have the coordinator role"));
		}

		if (coordinatorRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "User is already assigned as coordinator in this campaign"));
		}

		CoordinatorId coordinatorId = new CoordinatorId();
		coordinatorId.setIdUser(userId);
		coordinatorId.setIdCampaign(campaignId);

		// Persist relation entity with composite key and mapped references.
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

		logAudit("ASSIGN_COORDINATOR", authHeader, campaignId, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/{campaignId}/coordinators/{userId}")
	@Transactional
	public ResponseEntity<?> unassignCoordinator(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@PathVariable Integer userId) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		if (!campaignRepository.existsById(campaignId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		if (!coordinatorRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Assignment not found"));
		}

		coordinatorRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);

		logAudit("UNASSIGN_COORDINATOR", authHeader, campaignId, userId);
		return ResponseEntity.ok(Map.of("message", "Coordinator unassigned successfully"));
	}

	@PostMapping("/{campaignId}/captains")
	public ResponseEntity<?> assignCaptain(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@RequestBody(required = false) Map<String, Object> request) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		Integer userId = extractUserId(request);
		if (userId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
		}

		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "User not found"));
		}

		if (!userRepository.isCaptain(userId)) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "User does not have the captain role"));
		}

		if (captainRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "User is already assigned as captain in this campaign"));
		}

		CaptainId captainId = new CaptainId();
		captainId.setIdUser(userId);
		captainId.setIdCampaign(campaignId);

		// Persist relation entity with composite key and mapped references.
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

		logAudit("ASSIGN_CAPTAIN", authHeader, campaignId, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/{campaignId}/captains/{userId}")
	@Transactional
	public ResponseEntity<?> unassignCaptain(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@PathVariable Integer userId) {

		// RF-14 requires explicit admin verification at the start of every endpoint.
		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		if (!campaignRepository.existsById(campaignId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		if (!captainRepository.existsByIdIdUserAndIdIdCampaign(userId, campaignId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Assignment not found"));
		}

		captainRepository.deleteByIdIdUserAndIdIdCampaign(userId, campaignId);

		logAudit("UNASSIGN_CAPTAIN", authHeader, campaignId, userId);
		return ResponseEntity.ok(Map.of("message", "Captain unassigned successfully"));
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
				.map(c -> c.getId() == null ? null : c.getId().getIdUser())
				.filter(id -> id != null)
				.collect(Collectors.toSet());

		return userRepository.findAllCoordinators().stream()
				.filter(user -> user != null && user.getIdUser() != null)
				.filter(user -> !assignedIds.contains(user.getIdUser()))
				.map(this::toUserMap)
				.collect(Collectors.toList());
	}

	private List<Map<String, Object>> getAvailableCaptainUsers(Integer campaignId) {
		Set<Integer> assignedIds = captainRepository.findByIdIdCampaign(campaignId).stream()
				.map(c -> c.getId() == null ? null : c.getId().getIdUser())
				.filter(id -> id != null)
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
			if (assignment == null || assignment.getId() == null || assignment.getId().getIdUser() == null) {
				continue;
			}

			Integer userId = assignment.getId().getIdUser();
			userRepository.findById(userId).ifPresent(user -> result.add(toUserMap(user)));
		}

		return result;
	}

	private List<Map<String, Object>> usersFromCaptains(List<Captain> assignments) {
		List<Map<String, Object>> result = new ArrayList<>();

		for (Captain assignment : assignments) {
			if (assignment == null || assignment.getId() == null || assignment.getId().getIdUser() == null) {
				continue;
			}

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

	private Integer extractUserId(Map<String, Object> request) {
		// Accept Integer, numeric wrappers, or trimmed string userId values.
		if (request == null) {
			return null;
		}

		Object raw = request.get("userId");
		if (raw instanceof Integer i) {
			return i;
		}
		if (raw instanceof Number n) {
			return n.intValue();
		}
		if (raw instanceof String s) {
			String trimmed = trimToNull(s);
			if (trimmed == null) {
				return null;
			}
			try {
				return Integer.valueOf(trimmed);
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		return null;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private void logAudit(String action, String authHeader, Integer campaignId, Integer affectedUserId) {
		// Standardized audit format consumed by operations/security analysis.
		auditLog.info("ACTION={} adminUserId={} timestamp={} campaignId={} affectedUserId={}",
				action, adminGuard.extractUserId(authHeader), Instant.now(), campaignId, affectedUserId);
	}
}
