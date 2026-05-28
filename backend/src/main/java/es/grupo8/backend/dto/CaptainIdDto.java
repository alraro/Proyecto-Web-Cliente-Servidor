package es.grupo8.backend.dto;


//DTO representing the composite key for a captain-campaign assignment.

public record CaptainIdDto(
        Integer idUser,
        Integer idCampaign
) {}
