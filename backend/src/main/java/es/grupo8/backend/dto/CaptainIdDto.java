/**
 * DTO con el identificador compuesto de capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 95%
 * - IA Generativa: 5%
 */
package es.grupo8.backend.dto;


//DTO representing the composite key for a captain-campaign assignment.

public record CaptainIdDto(
        Integer idUser,
        Integer idCampaign
) {}
