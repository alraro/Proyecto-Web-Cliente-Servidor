package es.grupo8.backend.dto;

import lombok.Data;

// DTO for returning captain-campaign assignment data.
@Data
public class CaptainResponseDto {
    private Integer idUser;
    private String userName;
    private String userEmail;
    private Integer idCampaign;
    private String campaignName;
}
