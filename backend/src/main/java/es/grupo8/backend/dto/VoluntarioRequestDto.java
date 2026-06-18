/**
 * Autores:
 * - Alfonso Ramos Rojas: 100%
 */
package es.grupo8.backend.dto;

import java.util.List;

public record VoluntarioRequestDto(
        String name,
        String phone,
        String email,
        String address,
        List<Integer> campaignIds
) {
}