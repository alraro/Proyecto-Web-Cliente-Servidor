package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.AvailableVolunteerDto;
import es.grupo8.backend.entity.Volunteer;

@Component
public class AvailableVolunteerMapper extends MapperDTO<AvailableVolunteerDto, Volunteer> {

    @Override
    public AvailableVolunteerDto toDTO(Volunteer v) {
        if (v == null) return null;

        AvailableVolunteerDto dto = new AvailableVolunteerDto();
        dto.setVolunteerId(v.getId());
        dto.setName(v.getName());
        dto.setEmail(v.getEmail());
        dto.setPhone(v.getPhone());
        dto.setPartnerEntityName(v.getIdPartnerEntity() != null ? v.getIdPartnerEntity().getName() : null);
        return dto;
    }
}
