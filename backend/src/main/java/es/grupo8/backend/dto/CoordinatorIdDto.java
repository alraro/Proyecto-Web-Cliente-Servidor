/**
 * DTO con el identificador compuesto de coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 95%
 * - IA Generativa: 5%
 */
package es.grupo8.backend.dto;

//DTO representing the composite key for a coordinator-campaign assignment.
public record CoordinatorIdDto(
        Integer idUser,
        Integer idCampaign
) {}
