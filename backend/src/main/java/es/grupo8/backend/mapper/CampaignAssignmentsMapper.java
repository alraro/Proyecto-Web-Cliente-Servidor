/**
 * Mapeador entre campaña con asignaciones y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import java.util.List;

import es.grupo8.backend.dto.CampaignAssignmentsDTO;
import es.grupo8.backend.dto.UserResponseDto;
import es.grupo8.backend.entity.Campaign;
import org.springframework.stereotype.Component;

@Component
public class CampaignAssignmentsMapper extends MapperDTO<CampaignAssignmentsDTO, Campaign> {

    @Override
    public CampaignAssignmentsDTO toDTO(Campaign campaign) {
        return toDTO(campaign, List.of(), List.of());
    }

    public CampaignAssignmentsDTO toDTO(Campaign campaign, List<UserResponseDto> coordinators, List<UserResponseDto> captains) {
        if (campaign == null) return null;
        CampaignAssignmentsDTO dto = new CampaignAssignmentsDTO();
        dto.setCampaignId(campaign.getId());
        dto.setCampaignName(campaign.getName());
        dto.setCoordinators(coordinators);
        dto.setCaptains(captains);
        return dto;
    }
}
