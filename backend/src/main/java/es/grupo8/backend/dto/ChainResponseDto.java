package es.grupo8.backend.dto;

public record ChainResponseDto(
        Integer id,
        String name,
        String code,
        Boolean participation
) {}