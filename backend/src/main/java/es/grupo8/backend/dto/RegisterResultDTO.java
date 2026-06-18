/**
 * DTO con el resultado de un registro.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class RegisterResultDTO {
    private String message;
    private Integer requestId;
}
