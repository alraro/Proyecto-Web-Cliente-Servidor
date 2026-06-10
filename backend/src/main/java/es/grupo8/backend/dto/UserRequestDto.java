package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class UserRequestDto {

    private String name;
    private String email;
    private String phone;
    private String address;
    private String postalCode;
    private String password;
    private String role;
}
