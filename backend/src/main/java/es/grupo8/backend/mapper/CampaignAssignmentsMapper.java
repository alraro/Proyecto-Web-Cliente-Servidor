/**
 * Mapeador entre campaña con asignaciones y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import java.util.List;

import es.grupo8.backend.dto.CampaignAssignmentsDTO;
import es.grupo8.backend.dto.UserDTO;
import es.grupo8.backend.entity.Campaign;
import org.springframework.stereotype.Component;

/**
 * Maps a {@link Campaign} together with its coordinator and captain lists to {@link CampaignAssignmentsDTO}.
 * Use {@link #toDTO(Campaign, List, List)} when the assignment lists are available.
 */
@Component
public class CampaignAssignmentsMapper extends MapperDTO<CampaignAssignmentsDTO, Campaign> {

    @Override
    public CampaignAssignmentsDTO toDTO(Campaign campaign) {
        return toDTO(campaign, List.of(), List.of());
    }

    /**
     * Maps the campaign together with its pre-resolved coordinator and captain lists.
     *
     * @param campaign     the campaign entity
     * @param coordinators users assigned as coordinators
     * @param captains     users assigned as captains
     * @return populated DTO
     */
    public CampaignAssignmentsDTO toDTO(Campaign campaign, List<UserDTO> coordinators, List<UserDTO> captains) {
        if (campaign == null) return null;
        CampaignAssignmentsDTO dto = new CampaignAssignmentsDTO();
        dto.setCampaignId(campaign.getId());
        dto.setCampaignName(campaign.getName());
        dto.setCoordinators(coordinators);
        dto.setCaptains(captains);
        return dto;
    }
}
