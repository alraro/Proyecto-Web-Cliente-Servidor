package es.grupo8.backend.dto;

import lombok.Data;

//DTO for assigning a user as coordinator to a campaign.
@Data
public class CoordinatorAssignRequestDto {
    private Integer idUser;
    private Integer idCampaign;
}
