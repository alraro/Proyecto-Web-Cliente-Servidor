/**
 * DTO para exportar datos de cadenas.
 *
 * Autores:
 * - Hugo Herrero González: 90%
 * - Fernando Luis Pinilla Molina: 5%
 * - IA Generativa: 5%
 */
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportChainDTO {
    private Integer id;
    private String name;
    private String code;
    private String participation;
}