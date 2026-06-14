/**
 *
 * Autores:
 * - Hugo Herrero González: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.AuthResponseDTO;
import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.entity.UserEntity;


@Component
public class UserAuthMapper extends MapperDTO<AuthResponseDTO, UserEntity> {
    @Autowired
    private ProfileMapper profileMapper;

    @Override
    public AuthResponseDTO toDTO(UserEntity user) {
        AuthResponseDTO dto = new AuthResponseDTO();
        dto.setId(user.getIdUser());
        dto.setNombre(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public AuthResponseDTO toLoginResponse(UserEntity user, String token, String role, String redirectUrl, Long expiresInSeconds, Integer storeId) {
        AuthResponseDTO dto = toDTO(user);
        dto.setRole(role);
        dto.setRedirectUrl(redirectUrl);
        dto.setMessage("Login correcto");
        dto.setToken(token);
        dto.setTokenType("Bearer");
        dto.setExpiresInSeconds(expiresInSeconds);
        dto.setStoreId(storeId);
        return dto;
    }

    public AuthResponseDTO toRegisterResponse(UserEntity user, String token, Long expiresInSeconds) {
        AuthResponseDTO dto = toDTO(user);
        dto.setMessage("Registro correcto");
        dto.setToken(token);
        dto.setTokenType("Bearer");
        dto.setExpiresInSeconds(expiresInSeconds);
        return dto;
    }

    public ProfileDTO toProfileDTO(UserEntity user, String role, String redirectUrl, String message) {
        ProfileDTO dto = profileMapper.toDTO(user);
        dto.setRole(role);
        dto.setRedirectUrl(redirectUrl);
        dto.setMessage(message);
        return dto;
    }
}
