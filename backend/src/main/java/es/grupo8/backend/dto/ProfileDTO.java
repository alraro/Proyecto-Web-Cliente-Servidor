package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class ProfileDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String domicilio;
    private String cp;
    private String role;
    private String redirectUrl;
    private String message;
}
