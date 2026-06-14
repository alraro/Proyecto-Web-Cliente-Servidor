/**
 * Mapeador para la exportación de campañas.
 *
 * Autores:
 * - Hugo Herrero González: 95%
 * - Fernando Luis Pinilla Molina: 5%
 */
package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ExportCampaignDTO;
import es.grupo8.backend.entity.Campaign;

@Component
public class ExportCampaignMapper extends MapperDTO<ExportCampaignDTO, Campaign> {
    
    @Override
    public ExportCampaignDTO toDTO(Campaign c) {
        if (c == null) return null;

        ExportCampaignDTO dto = new ExportCampaignDTO();

        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setType(c.getIdType() != null ? c.getIdType().getName() : "");
        dto.setStartDate(c.getStartDate() != null ? c.getStartDate().toString() : "");
        dto.setEndDate(c.getEndDate() != null ? c.getEndDate().toString() : "");
        
        return dto;
    }
}