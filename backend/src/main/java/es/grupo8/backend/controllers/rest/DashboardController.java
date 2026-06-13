/**
 * Controlador REST del panel principal (resumen y métricas).
 *
 * Autores:
 * - Hugo Herrero González: 80%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.controllers.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CoverageItemDTO;
import es.grupo8.backend.services.DashboardService;


@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseRestController {

    @Autowired
    private DashboardService dashboardService;


    // Devolver listado de campañas con su cobertura total (para el dashboard general)
    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignDTO>> getAllCampaignsCoverage(){
        List<CampaignDTO> result = dashboardService.getAllCampaignsCoverage();

        return ResponseEntity.ok(result);
    }


    // Cobertura por cadena
    @GetMapping("/campaigns/{id}/coverage/chain")
    public ResponseEntity<List<CoverageItemDTO>> coverageByChain(@PathVariable Integer id){
        List<CoverageItemDTO> result = dashboardService.getCoverageByChain(id);

        return ResponseEntity.ok(result);
    }

    // Cobertura por localidad
    @GetMapping("/campaigns/{id}/coverage/locality")
    public ResponseEntity<List<CoverageItemDTO>> coverageByLocality(@PathVariable Integer id){
        List<CoverageItemDTO> result = dashboardService.getCoverageByLocality(id);

        return ResponseEntity.ok(result);
    }

    // Cobertura por zona geográfica
    @GetMapping("/campaigns/{id}/coverage/zone")
    public ResponseEntity<List<CoverageItemDTO>> coverageByZone(@PathVariable Integer id){
        List<CoverageItemDTO> result = dashboardService.getCoverageByZone(id);
        return ResponseEntity.ok(result);
    }
}