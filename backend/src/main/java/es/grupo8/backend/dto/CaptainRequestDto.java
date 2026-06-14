/**
 * DTO de entrada para solicitar el alta de un capitán.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

//DTO for submitting a captain application request.
 
@Data
public class CaptainRequestDto {
    private String name;
    private String email;
    private String password;
    private Integer campaignId;
}
