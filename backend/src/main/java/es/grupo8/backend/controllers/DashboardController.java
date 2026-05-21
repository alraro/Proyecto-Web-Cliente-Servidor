package es.grupo8.backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dto.CampaignSummaryDTO;
import es.grupo8.backend.dto.CoverageItemDTO;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;


    // Devolver listado de campañas con su cobertura total (para el dashboard general)
    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllCampaignsCoverage(){
        List<Object[]> rows = campaignStoreRepository.coverageAllCampaigns();
        List<CampaignSummaryDTO> result = rows.stream().map(r -> new CampaignSummaryDTO(
            (Integer) r[0], // id
            (String) r[1],  // name
            (java.time.LocalDate) r[2],
            (java.time.LocalDate) r[3],
            ((Number) r[4]).longValue()     // Tiendas en Campaña
        ))
        .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    // Cobertura por cadena
    @GetMapping("/campaigns/{id}/coverage/chain")
    public ResponseEntity<?> coverageByChain(@PathVariable Integer id){
        return buildCoverageResponse(campaignStoreRepository.coverageByChain(id));
    }

    // Cobertura por localidad
    @GetMapping("/campaigns/{id}/coverage/locality")
    public ResponseEntity<?> coverageByLocality(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String auth){
        return buildCoverageResponse(campaignStoreRepository.coverageByLocality(id));
    }

    // Cobertura por zona geográfica
    @GetMapping("/campaigns/{id}/coverage/zone")
    public ResponseEntity<?> coverageByZone(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String auth){
        return buildCoverageResponse(campaignStoreRepository.coverageByZone(id));
    }

    // Método auxiliar para construir la respuesta de cobertura
    private ResponseEntity<?> buildCoverageResponse(List<Object[]> rows){
        List<CoverageItemDTO> result = rows.stream().map(r -> new CoverageItemDTO(
            (String) r[0],  // label (cadena/localidad/zona)
            ((Number) r[1]).longValue(), // totalStores
            ((Number) r[2]).longValue()  // storesInCampaign
        ))
        .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


}