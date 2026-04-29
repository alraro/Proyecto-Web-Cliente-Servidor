package es.grupo8.backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dto.CampaignSummaryDTO;
import es.grupo8.backend.entity.Store;
import es.grupo8.backend.security.CoordinatorGuard;

@RestController
@RequestMapping("/api")
public class CampaignController {

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Autowired
    private CoordinatorGuard coordinatorGuard;

    /**
     * Get all campaigns - accessible by Coordinator role
     */
    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllCampaigns(
            @RequestHeader(value = "Authorization", required = false) String auth) {

        if (auth == null || !coordinatorGuard.isCoordinator(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Coordinator role required");
        }

        List<Object[]> rows = campaignStoreRepository.coverageAllCampaigns();
        List<CampaignSummaryDTO> result = rows.stream().map(r -> new CampaignSummaryDTO(
                (Integer) r[0], // id
                (String) r[1],  // name
                (java.time.LocalDate) r[2],
                (java.time.LocalDate) r[3],
                ((Number) r[4]).longValue() // storesInCampaign
        )).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Get all stores for a specific campaign
     */
    @GetMapping("/campaigns/{id}/stores")
    public ResponseEntity<?> getCampaignStores(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String auth) {

        if (auth == null || !coordinatorGuard.isCoordinator(auth, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: Coordinator role required for this campaign");
        }

        List<es.grupo8.backend.entity.CampaignStore> campaignStores = 
                campaignStoreRepository.findByIdCampaign_Id(id);

        List<Store> stores = campaignStores.stream()
                .map(cs -> cs.getIdStore())
                .collect(Collectors.toList());

        return ResponseEntity.ok(stores);
    }
}