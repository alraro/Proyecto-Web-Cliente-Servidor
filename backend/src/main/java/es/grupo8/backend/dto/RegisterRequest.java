package es.grupo8.backend.dto;

public record RegisterRequest(
        String nombre,
        String email,
        String password,
        String telefono,
        String domicilio,
        String localidad,
        String cp
) {
}
