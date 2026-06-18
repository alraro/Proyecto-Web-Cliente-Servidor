/**
 * DTO de salida para tipos de campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

// DTO for returning campaign type data.
@Data
public class CampaignTypeResponseDto {
    private Integer id;
    private String name;
}
