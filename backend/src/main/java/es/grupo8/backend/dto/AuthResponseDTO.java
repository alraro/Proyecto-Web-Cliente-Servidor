package es.grupo8.backend.dto;
import lombok.Data;

@Data
public class AuthResponseDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String password;

    private String phone;
    private String address;
    private String postalCode;

    private String role;
    private String redirectUrl;
    private Integer storeId;

    private String message;
    private String token;
    private String tokenType;
    private Long expiresInSeconds;
}
