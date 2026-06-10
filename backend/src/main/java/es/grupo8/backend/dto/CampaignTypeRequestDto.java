/**
 * DTO de entrada para tipos de campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 95%
 * - IA Generativa: 5%
 */
package es.grupo8.backend.dto;

import lombok.Data;


// DTO for creating or updating a campaign type.
@Data
public class CampaignTypeRequestDto {
    private String name;
}
