/**
 * DTO de petición para crear/editar cadenas.
 *
 * Autores:
 * - Alejandra Ortiz: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class ChainRequestDto {

    private String name;
    private String code;
    private Boolean participation;
}
