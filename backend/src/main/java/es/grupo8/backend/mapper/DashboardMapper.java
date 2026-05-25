package es.grupo8.backend.mapper;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.CampaignSummaryDTO;
import es.grupo8.backend.dto.CoverageItemDTO;

@Component
public class DashboardMapper {
    
    // Transforma fila al dto de campaña
    public CampaignSummaryDTO toCampaignSummaryDTO(Object[] row) {
        if(row == null) return null;

        CampaignSummaryDTO dto = new CampaignSummaryDTO();

        dto.setId((Integer) row[0]);
        dto.setName((String) row[1]);
        dto.setStartDate((LocalDate) row[2]);
        dto.setEndDate((LocalDate) row[3]);
        dto.setStoresInCampaign(((Number) row[4]).longValue());
        
        return dto;
    }

    // Transforma lista de filas a lista de dto de campaña
    public List<CampaignSummaryDTO> toCampaignSummaryDTOList(List<Object[]> rows) {
        if(rows == null) return null;
        return rows.stream().map(this::toCampaignSummaryDTO).collect(Collectors.toList());    }

    // Transforma fila al dto de cobertura
    public CoverageItemDTO toCoverageItemDTO(Object[] row){
        if(row == null) return null;

        CoverageItemDTO dto = new CoverageItemDTO();

        dto.setLabel((String) row[0]);
        dto.setTotalStores(((Number) row[1]).longValue());
        dto.setStoresInCampaign(((Number) row[2]).longValue());
        return dto;
    }

    // Transforma lista de filas a lista de dto de cobertura
    public List<CoverageItemDTO> toCoverageItemDTOList(List<Object[]> rows){
        if(rows == null) return null;
        return rows.stream().map(this::toCoverageItemDTO).collect(Collectors.toList());    
    }


}
