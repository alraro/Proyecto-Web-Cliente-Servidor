package es.grupo8.backend.dto;

public record UserRoleResponseDto(
        String message,
        Integer userId,
        String role
) {
}
