package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.AvailableCaptainDto;
import es.grupo8.backend.entity.UserEntity;

@Component
public class AvailableCaptainMapper extends MapperDTO<AvailableCaptainDto, UserEntity> {

    @Override
    public AvailableCaptainDto toDTO(UserEntity u) {
        if (u == null) return null;

        AvailableCaptainDto dto = new AvailableCaptainDto();
        dto.setUserId(u.getIdUser());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        return dto;
    }
}
