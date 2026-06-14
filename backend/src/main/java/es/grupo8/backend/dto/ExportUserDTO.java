/**
 * DTO para exportar datos de usuarios.
 *
 * Autores:
 * - Hugo Herrero González: 95%
 * - Fernando Luis Pinilla Molina: 5%
 */
package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ExportUserDTO {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
}