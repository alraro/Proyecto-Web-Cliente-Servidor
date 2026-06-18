/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CampaignInfoDto;
import es.grupo8.backend.entity.Campaign;
import org.springframework.stereotype.Component;

@Component
public class CampaignInfoMapper extends MapperDTO<CampaignInfoDto, Campaign> {

    @Override
    public CampaignInfoDto toDTO(Campaign campaign) {
        if (campaign == null) return null;

        CampaignInfoDto dto = new CampaignInfoDto();
        dto.setId(campaign.getId());
        dto.setName(campaign.getName());
        dto.setStartDate(campaign.getStartDate());
        dto.setEndDate(campaign.getEndDate());
        return dto;
    }

    public CampaignInfoDto toDTO(Campaign campaign, long volunteerCount) {
        CampaignInfoDto dto = toDTO(campaign);
        if (dto != null) {
            dto.setVolunteerCount(volunteerCount);
        }
        return dto;
    }
}
