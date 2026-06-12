/**
 * DTO de salida con datos de un coordinador.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

// DTO for returning coordinator-campaign assignment data.
@Data
public class CoordinatorResponseDto {
    private Integer idUser;
    private String userName;
    private String userEmail;
    private Integer idCampaign;
    private String campaignName;
}
