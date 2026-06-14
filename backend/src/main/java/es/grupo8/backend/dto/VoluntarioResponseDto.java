/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.dto;

import java.util.List;

public record VoluntarioResponseDto(
        Integer id,
        String name,
        String phone,
        String email,
        String address,
        Integer partnerEntityId,
        String partnerEntityName,
        List<CampaignInfo> campaigns
) {
    public record CampaignInfo(
            Integer id,
            String name
    ) {
    }
}