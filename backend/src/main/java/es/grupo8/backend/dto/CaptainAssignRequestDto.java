package es.grupo8.backend.dto;

import lombok.Data;

// DTO for assigning a user as captain to a campaign.
@Data
public class CaptainAssignRequestDto {
    private Integer idUser;
    private Integer idCampaign;
}
