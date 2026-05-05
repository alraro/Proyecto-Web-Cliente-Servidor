package es.grupo8.backend.dto;

/**
 * DTO for creating a new user with a specific role.
 */
public record UserRequestDto(
        String name,
        String email,
        String phone,
        String address,
        String postalCode,
        String password,
        String role
) {
}
