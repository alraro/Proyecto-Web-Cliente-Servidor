package es.grupo8.backend.dto;

import java.util.List;

/**
 * DTO for returning user data with assigned roles.
 */
public record UserResponseDto(
        Integer idUser,
        String name,
        String email,
        String phone,
        String address,
        String postalCode,
        List<String> roles
) {
}
