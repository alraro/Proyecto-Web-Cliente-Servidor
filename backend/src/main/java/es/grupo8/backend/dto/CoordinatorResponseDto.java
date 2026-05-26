package es.grupo8.backend.dto;

// DTO for returning coordinator-campaign assignment data.
public record CoordinatorResponseDto(
        Integer idUser,
        String userName,
        String userEmail,
        Integer idCampaign,
        String campaignName
) {}
