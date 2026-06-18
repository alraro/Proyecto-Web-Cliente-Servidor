/**
 * DTO con los coordinadores y capitanes asignados a una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CampaignAssignmentsDTO {

    private Integer campaignId;

    private String campaignName;

    private List<UserResponseDto> coordinators;

    private List<UserResponseDto> captains;
}
