/*
*
* Autores:
*	- Hugo Herrero González: 95%
*  - IA Generativa: 5%
*/
package es.grupo8.backend.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.CampaignDTO;

// Mapper para transformar los resultados del dashboard a DTO, solo lo usamos para el dashboard de campañas general
@Component
public class CampaignDashboardMapper extends MapperDTO<CampaignDTO, Object[]>{

    @Override
    public CampaignDTO toDTO(Object[] row) {
        if(row == null) return null;

        CampaignDTO dto = new CampaignDTO();


        dto.setId((Integer) row[0]);
        dto.setName((String) row[1]);
        dto.setStartDate((LocalDate) row[2]);
        dto.setEndDate((LocalDate) row[3]);
        dto.setStoresInCampaign(((Number) row[4]).longValue());
        return dto;
    }
    
}
