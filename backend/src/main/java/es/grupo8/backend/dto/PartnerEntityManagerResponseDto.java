package es.grupo8.backend.dto;

public record PartnerEntityManagerResponseDto(
        Integer userId,
        String name,
        String email,
        String phone,
        String address,
        String postalCode,
        Integer partnerEntityId,
        String partnerEntityName
) {
}
