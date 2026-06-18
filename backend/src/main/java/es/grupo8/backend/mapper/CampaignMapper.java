/**
 * Mapeador entre la entidad Campaign y CampaignDTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CampaignDTO;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.CampaignType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CampaignMapper extends MapperDTO<CampaignDTO, Campaign> {

    private String nullSafe(String v) { return v != null ? v : ""; }

    @Override
    public CampaignDTO toDTO(Campaign campaign) {
        if (campaign == null) return null;
        CampaignType type = campaign.getIdType();
        CampaignDTO dto = new CampaignDTO();
        dto.setId(campaign.getId());
        dto.setName(campaign.getName());
        dto.setStartDate(campaign.getStartDate());
        dto.setEndDate(campaign.getEndDate());
        dto.setTypeId(type != null ? type.getId() : null);
        dto.setTypeName(type != null ? nullSafe(type.getName()) : "");
        dto.setStoresInCampaign(campaign.getStores() != null ? campaign.getStores().size() : 0);
        dto.setStatus(computeStatus(campaign.getStartDate(), campaign.getEndDate()));
        return dto;
    }

    private static String computeStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (endDate != null && endDate.isBefore(today)) return "PAST";
        if (startDate != null && startDate.isAfter(today)) return "FUTURE";
        return "ACTIVE";
    }
}
