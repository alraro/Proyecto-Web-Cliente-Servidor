package es.grupo8.backend.dto;

//DTO representing the composite key for a coordinator-campaign assignment.
public record CoordinatorIdDto(
        Integer idUser,
        Integer idCampaign
) {}
