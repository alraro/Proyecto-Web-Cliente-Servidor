package es.grupo8.backend.dto;

/**
 * DTO for updating an existing user.
 * Email is optional for updates.
 */
public record UserUpdateRequestDto(
        String name,
        String email,
        String phone,
        String address,
        String postalCode,
        String password
) {
}
