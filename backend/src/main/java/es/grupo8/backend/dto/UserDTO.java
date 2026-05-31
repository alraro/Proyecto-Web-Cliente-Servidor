/**
 * DTO para transferir datos de usuarios.
 *
 * Autores:
 * - Alejandra Ortiz: 60%
 * - Alfonso Ramos: 40%
 */
package es.grupo8.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Entrada crear (POST) → name, email, phone, address, postalCode, password, role
 * Entrada editar (PUT) → name, email, phone, address, postalCode, password
 * Salida (GET) → idUser, name, email, phone, address, postalCode, roles
 *
 * Los campos que no apliquen en cada caso son null.
 * password nunca se devuelve en respuestas GET.
 */
@Data
public class UserDTO {

    // Solo salida GET
    private Integer idUser;
    private List<String> roles;

    // Entrada y salida
    private String name;
    private String email;
    private String phone;
    private String address;
    private String postalCode;

    // Solo entrada, nunca se devuelve
    private String password;
    private String role;
}