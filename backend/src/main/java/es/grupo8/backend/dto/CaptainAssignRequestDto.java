/**
 * DTO de entrada para asignar un capitán a una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 95%
 * - IA Generativa: 5%
 */
package es.grupo8.backend.dto;

import lombok.Data;

// DTO for assigning a user as captain to a campaign.
@Data
public class CaptainAssignRequestDto {
    private Integer idUser;
    private Integer idCampaign;
}
