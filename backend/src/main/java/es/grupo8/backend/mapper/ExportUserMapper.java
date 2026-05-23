package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.ExportUserDTO;
import es.grupo8.backend.entity.UserEntity;

@Component
public class ExportUserMapper extends MapperDTO<ExportUserDTO, UserEntity> {

    @Override
    public ExportUserDTO toDTO(UserEntity u) {
        if(u == null) return null;

        ExportUserDTO dto = new ExportUserDTO();

        dto.setId(u.getIdUser());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setPhone(nullSafe(u.getPhone()));
        dto.setAddress(nullSafe(u.getAddress()));
        dto.setPostalCode(nullSafe(u.getPostalCode()));

        return dto;
    }

    private String nullSafe(String v) { return v != null ? v : ""; }


}