/**
 * Mapeador entre entidad colaboradora de campaña y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CampaignEntityDTO;
import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.stereotype.Component;

@Component
public class CampaignEntityMapper extends MapperDTO<CampaignEntityDTO, PartnerEntity> {

    @Override
    public CampaignEntityDTO toDTO(PartnerEntity entity) {
        return toDTO(entity, 0L);
    }

    public CampaignEntityDTO toDTO(PartnerEntity entity, Long volunteerCount) {
        if (entity == null) return null;
        CampaignEntityDTO dto = new CampaignEntityDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setVolunteerCount(volunteerCount != null ? volunteerCount : 0L);
        return dto;
    }
}
