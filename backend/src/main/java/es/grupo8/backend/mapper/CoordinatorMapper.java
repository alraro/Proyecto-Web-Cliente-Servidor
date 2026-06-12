/**
 * Mapeador entre la asignación de coordinador y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CoordinatorResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CoordinatorMapper extends MapperDTO<CoordinatorResponseDto, Coordinator> {

    private String nullSafe(String v) { return v != null ? v : ""; }

    @Override
    public CoordinatorResponseDto toDTO(Coordinator entity) {
        if (entity == null) return null;
        UserEntity user = entity.getIdUser();
        Campaign campaign = entity.getIdCampaign();
        CoordinatorResponseDto dto = new CoordinatorResponseDto();
        dto.setIdUser(user.getIdUser());
        dto.setUserName(nullSafe(user.getName()));
        dto.setUserEmail(nullSafe(user.getEmail()));
        dto.setIdCampaign(campaign.getId());
        dto.setCampaignName(nullSafe(campaign.getName()));
        return dto;
    }
}
