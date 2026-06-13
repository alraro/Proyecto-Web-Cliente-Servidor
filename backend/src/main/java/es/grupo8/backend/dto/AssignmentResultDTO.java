/**
 * DTO con el resultado de una asignación a campaña.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class AssignmentResultDTO {
    private String message;
    private Integer campaignId;
    private Integer userId;
    private String userName;
}
