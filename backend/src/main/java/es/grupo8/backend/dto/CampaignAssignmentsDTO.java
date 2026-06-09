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
    private List<UserDTO> coordinators;

    /** Users assigned as captains. */
    private List<UserDTO> captains;
}
