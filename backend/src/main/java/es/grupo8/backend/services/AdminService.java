package es.grupo8.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignRepository;
import es.grupo8.backend.dao.IncidentRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.AdminDTO;
import es.grupo8.backend.dto.IncidentDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.mapper.AdminMapper;
import es.grupo8.backend.mapper.IncidentMapper;

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

    @Autowired
    private IncidentMapper incidentMapper;

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

    public List<IncidentDTO> getAllIncidents() {
        return incidentMapper.toDTOList(incidentRepository.findAll());
    }

}