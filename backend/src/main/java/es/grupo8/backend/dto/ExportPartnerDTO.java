/**
 * DTO para exportar datos de entidades colaboradoras.
 *
 * Autores:
 * - Hugo Herrero González: 95%
 * - Fernando Luis Pinilla Molina: 5%
 */
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportPartnerDTO {
    private Integer id;
    private String name;
    private String address;
    private String phone;
}