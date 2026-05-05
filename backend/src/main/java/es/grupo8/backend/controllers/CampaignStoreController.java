package es.grupo8.backend.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.security.AdminGuard;

/**
 * RF-12: administration of campaign-store assignments.
 *
 * Endpoints:
 * GET /api/campaigns/{campaignId}/stores
 * POST /api/campaigns/{campaignId}/stores
 * DELETE /api/campaigns/{campaignId}/stores/{storeId}
 * PUT /api/campaigns/{campaignId}/stores
 */
@RestController
public class CampaignStoreController {

	private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

	@Autowired
	private AdminGuard adminGuard;

	@Autowired
	private CampaignRepository campaignRepository;

	@Autowired
	private StoreRepository storeRepository;

	@Autowired
	private CampaignStoreRepository campaignStoreRepository;

	@GetMapping("/api/campaigns/{campaignId}/stores")
	public ResponseEntity<?> getCampaignStores(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId) {

		if (!adminGuard.isAdmin(authHeader)) {
			return forbidden();
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		List<StoreResponseDto> stores = new ArrayList<>();
		for (CampaignStore campaignStore : campaignStoreRepository.findByIdCampaign_Id(campaignId)) {
			if (campaignStore != null && campaignStore.getIdStore() != null) {
				stores.add(StoreController.toDto(campaignStore.getIdStore()));
			}
		}

		auditLog.info("ACTION=GET_CAMPAIGN_STORES adminUserId={} timestamp={} campaignId={}",
				adminGuard.extractUserId(authHeader), Instant.now(), campaignId);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("campaignId", campaign.getId());
		response.put("campaignName", campaign.getName());
		response.put("totalStores", stores.size());
		response.put("stores", stores);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/api/campaigns/{campaignId}/stores")
	public ResponseEntity<?> assignStoreToCampaign(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@RequestBody(required = false) Map<String, Object> request) {

		if (!adminGuard.isAdmin(authHeader)) {
			return forbidden();
		}

		Integer storeId = valueAsInteger(request == null ? null : request.get("storeId"));
		if (storeId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "storeId is required"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		Store store = storeRepository.findById(storeId).orElse(null);
		if (store == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Store not found"));
		}

		if (campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Esta tienda ya está asignada a la campaña"));
		}

		CampaignStoreId id = new CampaignStoreId();
		id.setIdCampaign(campaignId);
		id.setIdStore(storeId);

		CampaignStore campaignStore = new CampaignStore();
		campaignStore.setId(id);
		campaignStore.setIdCampaign(campaign);
		campaignStore.setIdStore(store);
		campaignStoreRepository.save(campaignStore);

		auditLog.info("ACTION=ASSIGN_STORE_TO_CAMPAIGN adminUserId={} timestamp={} campaignId={} storeId={}",
				adminGuard.extractUserId(authHeader), Instant.now(), campaignId, storeId);

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("message", "Tienda asignada correctamente");
		response.put("campaignId", campaignId);
		response.put("storeId", storeId);
		response.put("storeName", store.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@DeleteMapping("/api/campaigns/{campaignId}/stores/{storeId}")
	@Transactional
	public ResponseEntity<?> removeStoreFromCampaign(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@PathVariable Integer storeId) {

		if (!adminGuard.isAdmin(authHeader)) {
			return forbidden();
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		if (!campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Esta tienda no está asignada a la campaña"));
		}

		campaignStoreRepository.deleteByIdCampaign_IdAndIdStore_Id(campaignId, storeId);

		auditLog.info("ACTION=REMOVE_STORE_FROM_CAMPAIGN adminUserId={} timestamp={} campaignId={} storeId={}",
				adminGuard.extractUserId(authHeader), Instant.now(), campaignId, storeId);

		return ResponseEntity.ok(Map.of("message", "Tienda desasignada correctamente"));
	}

	@PutMapping("/api/campaigns/{campaignId}/stores")
	@Transactional
	public ResponseEntity<?> replaceCampaignStores(
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@PathVariable Integer campaignId,
			@RequestBody(required = false) Map<String, Object> request) {

		if (!adminGuard.isAdmin(authHeader)) {
			return forbidden();
		}

		Object rawStoreIds = request == null ? null : request.get("storeIds");
		List<Integer> storeIds = valueAsIntegerList(rawStoreIds);
		if (storeIds == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "storeIds list is required"));
		}

		Campaign campaign = campaignRepository.findById(campaignId).orElse(null);
		if (campaign == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Campaign not found"));
		}

		List<Store> validatedStores = new ArrayList<>();
		for (Integer storeId : storeIds) {
			Store store = storeRepository.findById(storeId).orElse(null);
			if (store == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("message", "Tienda no encontrada: " + storeId));
			}
			validatedStores.add(store);
		}

		campaignStoreRepository.deleteByCampaignId(campaignId);

		for (Store store : validatedStores) {
			CampaignStoreId id = new CampaignStoreId();
			id.setIdCampaign(campaignId);
			id.setIdStore(store.getId());

			CampaignStore campaignStore = new CampaignStore();
			campaignStore.setId(id);
			campaignStore.setIdCampaign(campaign);
			campaignStore.setIdStore(store);
			campaignStoreRepository.save(campaignStore);
		}

		auditLog.info("ACTION=BULK_UPDATE_CAMPAIGN_STORES adminUserId={} timestamp={} campaignId={} storeIds={}",
				adminGuard.extractUserId(authHeader), Instant.now(), campaignId, storeIds.toString());

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("message", "Tiendas de la campaña actualizadas correctamente");
		response.put("campaignId", campaignId);
		response.put("totalStores", storeIds.size());
		return ResponseEntity.ok(response);
	}

	private static ResponseEntity<?> forbidden() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "Access restricted to administrators"));
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

	private static List<Integer> valueAsIntegerList(Object value) {
		if (!(value instanceof List<?> rawList)) {
			return null;
		}

		List<Integer> result = new ArrayList<>();
		for (Object item : rawList) {
			Integer parsed = valueAsInteger(item);
			if (parsed == null) {
				return null;
			}
			result.add(parsed);
		}
		return result;
	}
}