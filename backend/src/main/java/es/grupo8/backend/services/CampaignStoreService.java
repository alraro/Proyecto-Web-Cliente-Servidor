package es.grupo8.backend.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignStore;
import es.grupo8.backend.entity.CampaignStoreId;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.mapper.StoreMapper;
import lombok.AllArgsConstructor;

// RF-12: campaign–store assignment management, admin only.
@Service
@AllArgsConstructor
public class CampaignStoreService {

    private final CampaignRepository campaignRepository;
    private final StoreRepository storeRepository;
    private final CampaignStoreRepository campaignStoreRepository;
    private final StoreMapper storeMapper;

    public Map<String, Object> getCampaignStores(Integer adminUserId, Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Campaign not found"));

        List<StoreResponseDto> stores = new ArrayList<>();
        for (CampaignStore cs : campaignStoreRepository.findByIdCampaign_Id(campaignId)) {
            if (cs != null && cs.getIdStore() != null) {
                stores.add(storeMapper.toDTO(cs.getIdStore()));
            }
        }

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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Tiendas de la campaña actualizadas correctamente");
        response.put("campaignId", campaignId);
        response.put("totalStores", storeIds.size());
        return response;
    }

}
