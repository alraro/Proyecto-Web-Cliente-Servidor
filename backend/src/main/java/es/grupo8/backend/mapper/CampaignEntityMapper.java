/**
 * Mapeador entre entidad colaboradora de campaña y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CampaignEntityDTO;
import es.grupo8.backend.entity.PartnerEntity;
import org.springframework.stereotype.Component;

/**
 * Maps {@link PartnerEntity} to {@link CampaignEntityDTO}.
 * Use {@link #toDTO(PartnerEntity, Long)} when the volunteer count is available.
 */
@Component
public class CampaignEntityMapper extends MapperDTO<CampaignEntityDTO, PartnerEntity> {

    @Override
    public CampaignEntityDTO toDTO(PartnerEntity entity) {
        return toDTO(entity, 0L);
    }

    /**
     * Maps the entity together with a pre-computed volunteer count for a campaign.
     *
     * @param entity         partner entity
     * @param volunteerCount number of volunteers from this entity in the target campaign
     * @return populated DTO
     */
    public CampaignEntityDTO toDTO(PartnerEntity entity, Long volunteerCount) {
        if (entity == null) return null;
        return new CampaignEntityDTO(
                entity.getId(),
                entity.getName(),
                entity.getPhone(),
                volunteerCount != null ? volunteerCount : 0L
        );
    }
}
