package es.grupo8.backend.dto;

/**
 * Incident reported by a captain for a campaign store.
 *
 * @param id           incident identifier
 * @param description  incident description
 * @param createdAt    ISO timestamp when the incident was created
 * @param campaignName name of the campaign
 * @param storeName    name of the store
 */
public record IncidentDTO(
        Integer id,
        String description,
        String createdAt,
        String campaignName,
        String storeName
) {}
