package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String password;
    private String role;
    private String redirectUrl;
    private String message;
    private String token;
    private String tokenType;
    private Long expiresInSeconds;
    private Integer storeId;
}
