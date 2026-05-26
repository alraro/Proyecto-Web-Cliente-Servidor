package es.grupo8.backend.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Locality;
import es.grupo8.backend.entity.PostalCode;
import es.grupo8.backend.entity.Store;
import lombok.AllArgsConstructor;

// RF-12: campaign–store assignment management, admin only.
@Service
@AllArgsConstructor
public class CampaignStoreService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;

    public Map<String, Object> getCampaignStores(Integer adminUserId, Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<StoreResponseDto> stores = new ArrayList<>();
        for (CampaignStore cs : campaignStoreRepository.findByIdCampaign_Id(campaignId)) {
            if (cs != null && cs.getIdStore() != null) {
                stores.add(toDto(cs.getIdStore()));
            }
        }

        auditLog.info("ACTION=GET_CAMPAIGN_STORES adminUserId={} timestamp={} campaignId={}",
                adminUserId, Instant.now(), campaignId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("campaignId", campaign.getId());
        response.put("campaignName", campaign.getName());
        response.put("totalStores", stores.size());
        response.put("stores", stores);
        return response;
    }

    public Map<String, Object> assignStoreToCampaign(Integer adminUserId, Integer campaignId, Integer storeId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NoSuchElementException("Store not found"));

        if (campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
            throw new IllegalStateException("Esta tienda ya está asignada a la campaña");
        }

        CampaignStoreId csId = new CampaignStoreId();
        csId.setIdCampaign(campaignId);
        csId.setIdStore(storeId);

        CampaignStore cs = new CampaignStore();
        cs.setId(csId);
        cs.setIdCampaign(campaign);
        cs.setIdStore(store);
        campaignStoreRepository.save(cs);

        auditLog.info("ACTION=ASSIGN_STORE_TO_CAMPAIGN adminUserId={} timestamp={} campaignId={} storeId={}",
                adminUserId, Instant.now(), campaignId, storeId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Tienda asignada correctamente");
        response.put("campaignId", campaignId);
        response.put("storeId", storeId);
        response.put("storeName", store.getName());
        return response;
    }

    @Transactional
    public void removeStoreFromCampaign(Integer adminUserId, Integer campaignId, Integer storeId) {
        if (!campaignRepository.existsById(campaignId)) {
            throw new NoSuchElementException("Campaign not found");
        }
        if (!campaignStoreRepository.existsByIdCampaign_IdAndIdStore_Id(campaignId, storeId)) {
            throw new NoSuchElementException("Esta tienda no está asignada a la campaña");
        }
        campaignStoreRepository.deleteByIdCampaign_IdAndIdStore_Id(campaignId, storeId);
        auditLog.info("ACTION=REMOVE_STORE_FROM_CAMPAIGN adminUserId={} timestamp={} campaignId={} storeId={}",
                adminUserId, Instant.now(), campaignId, storeId);
    }

    @Transactional
    public Map<String, Object> replaceCampaignStores(Integer adminUserId, Integer campaignId, List<Integer> storeIds) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<Store> validatedStores = new ArrayList<>();
        for (Integer storeId : storeIds) {
            validatedStores.add(storeRepository.findById(storeId)
                    .orElseThrow(() -> new NoSuchElementException("Tienda no encontrada: " + storeId)));
        }

        campaignStoreRepository.deleteByCampaignId(campaignId);

        for (Store store : validatedStores) {
            CampaignStoreId csId = new CampaignStoreId();
            csId.setIdCampaign(campaignId);
            csId.setIdStore(store.getId());

            CampaignStore cs = new CampaignStore();
            cs.setId(csId);
            cs.setIdCampaign(campaign);
            cs.setIdStore(store);
            campaignStoreRepository.save(cs);
        }

        auditLog.info("ACTION=BULK_UPDATE_CAMPAIGN_STORES adminUserId={} timestamp={} campaignId={} storeIds={}",
                adminUserId, Instant.now(), campaignId, storeIds.toString());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Tiendas de la campaña actualizadas correctamente");
        response.put("campaignId", campaignId);
        response.put("totalStores", storeIds.size());
        return response;
    }

    private StoreResponseDto toDto(Store s) {
        String postalCode = null, locality = null, zone = null;
        Integer localityId = null, zoneId = null, chainId = null;
        String chainName = null;

        if (s.getPostalCode() != null) {
            PostalCode pc = s.getPostalCode();
            postalCode = pc.getPostalCode();
            if (pc.getIdLocality() != null) {
                Locality loc = pc.getIdLocality();
                locality   = loc.getName();
                localityId = loc.getId();
                if (loc.getIdZone() != null) {
                    zone   = loc.getIdZone().getName();
                    zoneId = loc.getIdZone().getId();
                }
            }
        }
        if (s.getIdChain() != null) {
            chainId   = s.getIdChain().getIdChain();
            chainName = s.getIdChain().getName();
        }

        return new StoreResponseDto(
                s.getId(), s.getName(), s.getAddress(),
                postalCode, locality, localityId, zone, zoneId,
                chainId, chainName);
    }
}
