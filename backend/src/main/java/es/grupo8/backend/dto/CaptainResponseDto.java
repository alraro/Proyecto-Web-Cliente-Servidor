package es.grupo8.backend.dto;


//DTO for returning captain-campaign assignment data. 
public record CaptainResponseDto(
        Integer idUser,
        String userName,
        String userEmail,
        Integer idCampaign,
        String campaignName
) {}
