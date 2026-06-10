/**
 * DTO de entidad colaboradora con voluntarios en una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.dto;

/**
 * Partner entity with the number of volunteers assigned to a specific campaign.
 *
 * @param id             entity identifier
 * @param name           entity name
 * @param phone          entity contact phone
 * @param volunteerCount number of volunteers from this entity in the campaign
 */
public record CampaignEntityDTO(
        Integer id,
        String name,
        String phone,
        Long volunteerCount
) {}
