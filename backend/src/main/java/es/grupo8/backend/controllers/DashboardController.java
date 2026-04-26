package es.grupo8.backend.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dao.CoordinatorRepository;
import es.grupo8.backend.dto.CampaignSummaryDTO;
import es.grupo8.backend.dto.CoverageItemDTO;
import es.grupo8.backend.security.AdminGuard;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Autowired
    private CoordinatorRepository coordinatorRepository;

    @Autowired
    private AdminGuard adminGuard;


    // Devolvemos userId si el token es válido y el usuario es admin o coordinador, o null si no
    private Integer requireAdminOrCoordinator(String authHeader){
        Integer userId = adminGuard.extractUserId(authHeader);
        if (userId == null){
            return null;
        }

        boolean isAdmin = adminGuard.isAdmin(authHeader);
        boolean isCoordinator = coordinatorRepository.existsByIdUser_IdUser(userId);

        if (!isAdmin && !isCoordinator){
            return -1;
        }

        return userId;
    }


    // Devolver listado de campañas con su cobertura total (para el dashboard general)
    @GetMapping("/campaigns")
    public ResponseEntity<?> getAllCampaignsCoverage(@RequestHeader(value = "Authorization", required = false) String auth){
        Integer userId = requireAdminOrCoordinator(auth);

        if(userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: valid token required");
        }
        if(userId == -1){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: admin or coordinator role required");
        }

        List<Object[]> rows = campaignStoreRepository.coverageAllCampaigns();
        List<CampaignSummaryDTO> result = rows.stream().map(r -> new CampaignSummaryDTO(
            (Integer) r[0], // id
            (String) r[1],  // name
            (java.time.LocalDate) r[2],
            (java.time.LocalDate) r[3],
            ((Number) r[4]).longValue()     // storesInCampaign
        ))
        .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    // Cobertura por cadena
    @GetMapping("/campaigns/{id}/coverage/chain")
    public ResponseEntity<?> coverageByChain(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String auth){
        
        return buildCoverageResponse(auth, id, campaignStoreRepository.coverageByChain(id));
    }

    // Cobertura por localidad
    @GetMapping("/campaigns/{id}/coverage/locality")
    public ResponseEntity<?> coverageByLocality(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String auth){
        
        return buildCoverageResponse(auth, id, campaignStoreRepository.coverageByLocality(id));
    }

    // Cobertura por zona geográfica
    @GetMapping("/campaigns/{id}/coverage/zone")
    public ResponseEntity<?> coverageByZone(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String auth){
        
        return buildCoverageResponse(auth, id, campaignStoreRepository.coverageByZone(id));
    }

    // Método auxiliar para construir la respuesta de cobertura
    private ResponseEntity<?> buildCoverageResponse(String auth, Integer campaignId, List<Object[]> rows){
        Integer userId = requireAdminOrCoordinator(auth);

        if(userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: valid token required");
        }
        if(userId == -1){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: admin or coordinator role required");
        }

        boolean isAdmin = adminGuard.isAdmin(auth);
        if(!isAdmin && !coordinatorRepository.existsByIdUser_IdUserAndIdCampaign_Id(userId, campaignId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: not coordinator of this campaign");
        }

        List<CoverageItemDTO> result = rows.stream().map(r -> new CoverageItemDTO(
            (String) r[0],  // label (cadena/localidad/zona)
            ((Number) r[1]).longValue(), // totalStores
            ((Number) r[2]).longValue()  // storesInCampaign
        ))
        .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


}