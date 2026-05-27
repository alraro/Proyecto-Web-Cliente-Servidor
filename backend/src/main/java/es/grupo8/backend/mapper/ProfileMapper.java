package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ProfileDTO;
import es.grupo8.backend.entity.UserEntity;

@Component
public class ProfileMapper extends MapperDTO<ProfileDTO, UserEntity> {
    @Override
    public ProfileDTO toDTO(UserEntity user) {
        ProfileDTO dto = new ProfileDTO();

        dto.setId(user.getIdUser());
        dto.setNombre(user.getName());
        dto.setEmail(user.getEmail());
        dto.setTelefono(user.getPhone() == null ? "" : user.getPhone());
        dto.setDomicilio(user.getAddress() == null ? "" : user.getAddress());
        dto.setCp(user.getPostalCode() == null ? "" : user.getPostalCode());
        return dto;
    }
}
