/**
 * Mapeador entre la asignación de capitán y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.CaptainResponseDto;
import es.grupo8.backend.entity.Campaign;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CaptainMapper extends MapperDTO<CaptainResponseDto, Captain> {

    private String nullSafe(String v) { return v != null ? v : ""; }

    @Override
    public CaptainResponseDto toDTO(Captain entity) {
        if (entity == null) return null;
        UserEntity user = entity.getIdUser();
        Campaign campaign = entity.getIdCampaign();
        CaptainResponseDto dto = new CaptainResponseDto();
        dto.setIdUser(user.getIdUser());
        dto.setUserName(nullSafe(user.getName()));
        dto.setUserEmail(nullSafe(user.getEmail()));
        dto.setIdCampaign(campaign.getId());
        dto.setCampaignName(nullSafe(campaign.getName()));
        return dto;
    }
}
