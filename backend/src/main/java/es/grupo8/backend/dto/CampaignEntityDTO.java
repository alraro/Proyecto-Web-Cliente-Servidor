/**
 * DTO de entidad colaboradora con voluntarios en una campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class CampaignEntityDTO {
    private Integer id;
    private String name;
    private String phone;
    private Long volunteerCount;
}
