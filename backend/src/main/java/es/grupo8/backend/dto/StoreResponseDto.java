package es.grupo8.backend.dto;

public record StoreResponseDto(
        Integer id,
        String name,
        String address,
        String postalCode,
        String locality,
        Integer localityId,
        String zone,
        Integer zoneId,
        Integer chainId,
        String chainName
) {}