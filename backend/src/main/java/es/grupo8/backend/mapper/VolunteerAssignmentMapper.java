package es.grupo8.backend.mapper;

import org.springframework.stereotype.Component;

import es.grupo8.backend.dto.VolunteerAssignmentDto;
import es.grupo8.backend.entity.VolunteerShift;

@Component
public class VolunteerAssignmentMapper extends MapperDTO<VolunteerAssignmentDto, VolunteerShift> {

    @Override
    public VolunteerAssignmentDto toDTO(VolunteerShift vs) {
        if (vs == null) return null;

        VolunteerAssignmentDto dto = new VolunteerAssignmentDto();
        dto.setVolunteerId(vs.getIdVolunteer().getId());
        dto.setName(vs.getIdVolunteer().getName());
        dto.setEmail(vs.getIdVolunteer().getEmail());
        dto.setPhone(vs.getIdVolunteer().getPhone());
        return dto;
    }
}
