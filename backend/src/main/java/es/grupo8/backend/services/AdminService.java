/**
 *
 * Autores:
 * - Hugo Herrero González: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.IncidentRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.AdminDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Incident;
import es.grupo8.backend.mapper.AdminMapper;

@Service
public class AdminService {

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AdminMapper adminMapper;

    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    public List<AdminDTO> getChainCoverage (Integer campaignId) {
        return storeRepository.findChainCoverageCampaign(campaignId).stream().map(
            row -> adminMapper.toCoverageDTO(
                (String) row.get("chainName"),
                ((Number) row.get("covered")).longValue(),
                ((Number) row.get("total")).longValue()
            )
        ).collect(Collectors.toList());
    }

    public List<AdminDTO> getLocalityCoverage (Integer campaignId) {
        return storeRepository.findLocalityCoverageCampaign(campaignId).stream().map(
            row -> adminMapper.toCoverageDTO(
                (String) row.get("localityName"),
                ((Number) row.get("covered")).longValue(),
                ((Number) row.get("total")).longValue()
            )
        ).collect(Collectors.toList());
    }

    public List<AdminDTO> getZoneCoverage (Integer campaignId) {
        return storeRepository.findZoneCoverageCampaign(campaignId).stream().map(
            row -> adminMapper.toCoverageDTO(
                (String) row.get("zoneName"),
                ((Number) row.get("covered")).longValue(),
                ((Number) row.get("total")).longValue()
            )
        ).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllIncidents(String dir) {
        List<Incident> incidents;

        if ("asc".equals(dir)){
            incidents = incidentRepository.findAllOrderByIdAsc();
        } else {
            incidents = incidentRepository.findAllOrderByIdDesc();
        }
        
        
        return incidents.stream().map(i ->{
            Map<String, Object> m = new HashMap<>();
            m.put("id", i.getId());
            m.put("description", i.getDescription());
            m.put("createdAt", i.getCreatedAt() != null ? i.getCreatedAt().toString() : "-");
            m.put("campaignName", i.getIdCampaign() != null ? i.getIdCampaign().getName() : "-");
            m.put("storeName", i.getIdStore() != null ? i.getIdStore().getName() : "-");
            m.put("captainName", i.getIdUser() != null ? i.getIdUser().getName() : "-");

            return m;
        }).collect(Collectors.toList());
    }

    public void deleteIncident (Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El id de la incidencia no existe");
        }


        if(incidentRepository.existsById(id)){
            incidentRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("No se encuentra esta incidencia");
        }
    }

}