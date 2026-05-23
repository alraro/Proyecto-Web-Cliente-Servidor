package es.grupo8.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dto.CampaignSummaryDTO;
import es.grupo8.backend.dto.CoverageItemDTO;
import es.grupo8.backend.mapper.DashboardMapper;


@Service
public class DashboardService {

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Autowired
    private DashboardMapper dashboardMapper;


    public List<CampaignSummaryDTO> getAllCampaignsCoverage() {
        List<Object[]> rows = campaignStoreRepository.coverageAllCampaigns();
        return dashboardMapper.toCampaignSummaryDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByChain(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByChain(campaignId);
        return dashboardMapper.toCoverageItemDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByLocality(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByLocality(campaignId);
        return dashboardMapper.toCoverageItemDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByZone(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByZone(campaignId);
        return dashboardMapper.toCoverageItemDTOList(rows);
    }





}