package es.grupo8.backend.dto;

public record LoginResponse(Integer userId, String nombre, String email, String message, String token, String tokenType) {
}
