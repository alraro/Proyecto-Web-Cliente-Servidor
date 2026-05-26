package es.grupo8.backend.dto;

import java.time.Instant;


public record CaptainRequestResponseDto(
        Integer id,
        String name,
        String email,
        Integer idCampaign,
        String campaignName,
        Integer idCoordinator,
        String coordinatorName,
        String status,
        Instant createdAt,
        Instant resolvedAt
) {}
