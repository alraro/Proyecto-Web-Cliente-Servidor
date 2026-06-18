/**
 * Servicio del panel principal con resúmenes y métricas.
 *
 * Autores:
 * - Hugo Herrero González: 80%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 15%
 */
package es.grupo8.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.grupo8.backend.dao.CampaignStoreRepository;
import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.dto.CoverageItemDTO;
import es.grupo8.backend.mapper.CampaignDashboardMapper;
import es.grupo8.backend.mapper.CoverageItemDashboardMapper;


@Service
public class DashboardService {

    @Autowired
    private CampaignStoreRepository campaignStoreRepository;

    @Autowired
    private CampaignDashboardMapper campaignDashboardMapper;

    @Autowired
    private CoverageItemDashboardMapper coverageItemDashboardMapper;


    public List<CampaignDTO> getAllCampaignsCoverage() {
        List<Object[]> rows = campaignStoreRepository.coverageAllCampaigns();
        return campaignDashboardMapper.toDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByChain(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByChain(campaignId);
        return coverageItemDashboardMapper.toDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByLocality(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByLocality(campaignId);
        return coverageItemDashboardMapper.toDTOList(rows);
    }

    public List<CoverageItemDTO> getCoverageByZone(Integer campaignId) {
        List<Object[]> rows = campaignStoreRepository.coverageByZone(campaignId);
        return coverageItemDashboardMapper.toDTOList(rows);
    }
}