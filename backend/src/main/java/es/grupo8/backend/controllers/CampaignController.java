package es.grupo8.backend.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CampaignTypeRepository;
import es.grupo8.backend.dao.CaptainRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignType;
import es.grupo8.backend.security.AdminGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Campaigns", description = "Campaign management — Admin only for write operations")
public class CampaignController {

	private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

	@Autowired
	private AdminGuard adminGuard;

	@Autowired
	private CampaignRepository campaignRepository;

	@Autowired
	private CampaignTypeRepository campaignTypeRepository;

	@Autowired
	private CoordinatorRepository coordinatorRepository;

	@Autowired
	private CaptainRepository captainRepository;

	@Autowired
	private CampaignStoreRepository campaignStoreRepository;

	@GetMapping("/campaign-types")
	@Operation(summary = "List all campaign types")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Campaign types listed successfully")
	})
	public ResponseEntity<?> getCampaignTypes() {
		List<Map<String, Object>> response = campaignTypeRepository.findAll().stream()
				.map(type -> Map.<String, Object>of(
						"id", type.getId(),
						"name", type.getName()))
				.collect(Collectors.toList());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/campaigns")
	@Operation(summary = "List campaigns with optional status filter and pagination")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Campaigns listed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid status value")
	})
	public ResponseEntity<?> getCampaigns(
			@RequestParam(required = false) String status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "startDate,desc") String sort) {

		if (status != null && !status.equalsIgnoreCase("ACTIVE")
				&& !status.equalsIgnoreCase("PAST")
				&& !status.equalsIgnoreCase("FUTURE")) {
			return ResponseEntity.badRequest()
					.body(Map.of("message", "Invalid status. Use ACTIVE, PAST or FUTURE"));
		}

		if (size > 50) {
			size = 50;
		}
		if (size < 1) {
			size = 10;
		}

		Sort sortObject = Sort.by(Sort.Direction.DESC, "startDate");
		String[] sortParts = sort == null ? new String[0] : sort.split(",");
		if (sortParts.length == 2) {
			String field = sortParts[0] == null ? "" : sortParts[0].trim();
			String direction = sortParts[1] == null ? "" : sortParts[1].trim();
			boolean allowedField = Arrays.asList("startDate", "endDate", "name").contains(field);
			if (allowedField) {
				if ("asc".equalsIgnoreCase(direction)) {
					sortObject = Sort.by(Sort.Direction.ASC, field);
				} else if ("desc".equalsIgnoreCase(direction)) {
					sortObject = Sort.by(Sort.Direction.DESC, field);
				}
			}
		}

		Pageable pageable = PageRequest.of(page, size, sortObject);

		LocalDate today = LocalDate.now();
		long totalActive = campaignRepository
				.countByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
		long totalPast = campaignRepository.countByEndDateBefore(today);
		long totalFuture = campaignRepository.countByStartDateAfter(today);

		Page<Campaign> campaignPage;
		if (status == null) {
			campaignPage = campaignRepository.findAll(pageable);
		} else {
			campaignPage = switch (status.toUpperCase()) {
				case "PAST" -> campaignRepository.findByEndDateBefore(today, pageable);
				case "FUTURE" -> campaignRepository.findByStartDateAfter(today, pageable);
				default -> campaignRepository
						.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(today, today, pageable);
			};
		}

		List<Map<String, Object>> content = campaignPage.getContent().stream()
				.map(this::toCampaignMap)
				.collect(Collectors.toList());

		Map<String, Object> pagination = new LinkedHashMap<>();
		pagination.put("page", campaignPage.getNumber());
		pagination.put("size", campaignPage.getSize());
		pagination.put("totalElements", campaignPage.getTotalElements());
		pagination.put("totalPages", campaignPage.getTotalPages());
		pagination.put("isFirst", campaignPage.isFirst());
		pagination.put("isLast", campaignPage.isLast());

		Map<String, Object> summary = new LinkedHashMap<>();
		summary.put("totalActive", totalActive);
		summary.put("totalPast", totalPast);
		summary.put("totalFuture", totalFuture);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("content", content);
		response.put("pagination", pagination);
		response.put("summary", summary);

		return ResponseEntity.ok(response);
	}

	@GetMapping("/campaigns/{id}")
	@Operation(summary = "Get a single campaign by id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Campaign found"),
			@ApiResponse(responseCode = "404", description = "Campaign not found")
	})
	public ResponseEntity<?> getCampaignById(@PathVariable Integer id) {
		Campaign campaign = campaignRepository.findById(id).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		return ResponseEntity.ok(toCampaignMap(campaign));
	}

	@PostMapping("/campaigns")
	@Operation(summary = "Create a new campaign — Admin only")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Campaign created successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error"),
			@ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
			@ApiResponse(responseCode = "404", description = "Campaign type not found"),
			@ApiResponse(responseCode = "409", description = "Campaign name conflict")
	})
	public ResponseEntity<?> createCampaign(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestBody(required = false) Map<String, Object> request) {

		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		String name = trimToNull(valueAsString(request == null ? null : request.get("name")));
		Integer typeId = valueAsInteger(request == null ? null : request.get("typeId"));
		LocalDate startDate = valueAsLocalDate(request == null ? null : request.get("startDate"));
		LocalDate endDate = valueAsLocalDate(request == null ? null : request.get("endDate"));

		if (name == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Campaign name is required"));
		}
		if (typeId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Campaign type is required"));
		}
		if (startDate == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Start date is required"));
		}
		if (endDate == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "End date is required"));
		}
		if (!endDate.isAfter(startDate)) {
			return ResponseEntity.badRequest().body(Map.of("message", "End date must be after start date"));
		}

		CampaignType campaignType = campaignTypeRepository.findById(typeId).orElse(null);
		if (campaignType == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign type not found"));
		}

		if (campaignRepository.existsByName(name)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "A campaign with this name already exists"));
		}

		Campaign campaign = new Campaign();
		campaign.setName(name);
		campaign.setIdType(campaignType);
		campaign.setStartDate(startDate);
		campaign.setEndDate(endDate);

		Campaign saved = campaignRepository.save(campaign);
		logAudit("CREATE_CAMPAIGN", authHeader, saved.getId(), saved.getName());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("message", "Campaign created successfully");
		response.put("campaign", toCampaignMap(saved));
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/campaigns/{id}")
	@Operation(summary = "Update an existing campaign — Admin only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Campaign updated successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error"),
			@ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
			@ApiResponse(responseCode = "404", description = "Campaign or campaign type not found"),
			@ApiResponse(responseCode = "409", description = "Campaign name conflict")
	})
	public ResponseEntity<?> updateCampaign(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer id,
			@RequestBody(required = false) Map<String, Object> request) {

		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		String name = trimToNull(valueAsString(request == null ? null : request.get("name")));
		Integer typeId = valueAsInteger(request == null ? null : request.get("typeId"));
		LocalDate startDate = valueAsLocalDate(request == null ? null : request.get("startDate"));
		LocalDate endDate = valueAsLocalDate(request == null ? null : request.get("endDate"));

		if (name == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Campaign name is required"));
		}
		if (typeId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Campaign type is required"));
		}
		if (startDate == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Start date is required"));
		}
		if (endDate == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "End date is required"));
		}
		if (!endDate.isAfter(startDate)) {
			return ResponseEntity.badRequest().body(Map.of("message", "End date must be after start date"));
		}

		Campaign campaign = campaignRepository.findById(id).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		CampaignType campaignType = campaignTypeRepository.findById(typeId).orElse(null);
		if (campaignType == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign type not found"));
		}

		if (campaignRepository.existsByNameAndIdNot(name, id)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "A campaign with this name already exists"));
		}

		campaign.setName(name);
		campaign.setIdType(campaignType);
		campaign.setStartDate(startDate);
		campaign.setEndDate(endDate);

		Campaign updated = campaignRepository.save(campaign);
		logAudit("UPDATE_CAMPAIGN", authHeader, updated.getId(), updated.getName());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("message", "Campaign updated successfully");
		response.put("campaign", toCampaignMap(updated));
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/campaigns/{id}")
	@Transactional
	@Operation(summary = "Delete a campaign and its assignments — Admin only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Campaign deleted successfully"),
			@ApiResponse(responseCode = "403", description = "Access restricted to administrators"),
			@ApiResponse(responseCode = "404", description = "Campaign not found")
	})
	public ResponseEntity<?> deleteCampaign(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer id) {

		if (!adminGuard.isAdmin(authHeader)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("message", "Access restricted to administrators"));
		}

		Campaign campaign = campaignRepository.findById(id).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		coordinatorRepository.deleteAllByIdIdCampaign(id);
		captainRepository.deleteAllByIdIdCampaign(id);
		// RF-12: remove store assignments before deleting the campaign
		campaignStoreRepository.deleteByIdCampaign_Id(id);
		campaignRepository.deleteById(id);

		logAudit("DELETE_CAMPAIGN", authHeader, id, campaign.getName());
		return ResponseEntity.ok(Map.of("message", "Campaign deleted successfully"));
	}

	private void logAudit(String action, String authHeader, Integer id, String name) {
		auditLog.info("ACTION={} adminUserId={} timestamp={} campaignId={} campaignName={}",
				action, adminGuard.extractUserId(authHeader), Instant.now(), id, name);
	}

	private Map<String, Object> toCampaignMap(Campaign campaign) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("id", campaign.getId());
		response.put("name", campaign.getName());
		response.put("type", campaignTypeToMap(campaign.getIdType()));
		response.put("startDate", campaign.getStartDate() == null ? null : campaign.getStartDate().toString());
		response.put("endDate", campaign.getEndDate() == null ? null : campaign.getEndDate().toString());
		response.put("status", computeStatus(campaign.getStartDate(), campaign.getEndDate()));
		return response;
	}

	private static String computeStatus(LocalDate startDate, LocalDate endDate) {
		LocalDate today = LocalDate.now();
		if (endDate != null && endDate.isBefore(today)) {
			return "PAST";
		}
		if (startDate != null && startDate.isAfter(today)) {
			return "FUTURE";
		}
		return "ACTIVE";
	}

	private Map<String, Object> campaignTypeToMap(CampaignType type) {
		if (type == null) {
			return null;
		}
		return Map.of(
				"id", type.getId(),
				"name", type.getName());
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static String valueAsString(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private static Integer valueAsInteger(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer i) {
			return i;
		}
		if (value instanceof Number n) {
			return n.intValue();
		}
		try {
			return Integer.valueOf(String.valueOf(value));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private static LocalDate valueAsLocalDate(Object value) {
		if (value == null) {
			return null;
		}
		try {
			return LocalDate.parse(String.valueOf(value));
		} catch (DateTimeParseException ex) {
			return null;
		}
	}
}
