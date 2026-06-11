package es.grupo8.backend.dto;

import java.util.List;

public record UserResponseDto(
        Integer idUser,
        List<String> roles,
        String name,
        String email,
        String phone,
        String address,
        String postalCode
) {
}
