/**
 * DTO con los coordinadores y capitanes asignados a una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Coordinator and captain assignments for a specific campaign.
 */
@Data
public class CampaignAssignmentsDTO {

    /** Campaign identifier. */
    private Integer campaignId;

    /** Campaign name. */
    private String campaignName;

    /** Users assigned as coordinators. */
    private List<UserResponseDto> coordinators;

    /** Users assigned as captains. */
    private List<UserResponseDto> captains;
}
