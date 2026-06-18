/**
 * Mapeador entre la entidad VolunteerShift y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.VolunteerShiftDTO;
import es.grupo8.backend.entity.VolunteerShift;
import org.springframework.stereotype.Component;

@Component
public class VolunteerShiftMapper extends MapperDTO<VolunteerShiftDTO, VolunteerShift> {

    @Override
    public VolunteerShiftDTO toDTO(VolunteerShift vs) {
        if (vs == null) return null;
        VolunteerShiftDTO dto = new VolunteerShiftDTO();
        dto.setVolunteerId(vs.getIdVolunteer().getId());
        dto.setVolunteerName(vs.getIdVolunteer().getName());
        dto.setPhone(vs.getIdVolunteer().getPhone());
        dto.setShiftDay(vs.getId().getShiftDay());
        dto.setStartTime(vs.getId().getStartTime());
        dto.setEndTime(vs.getEndTime());
        dto.setAttendance(vs.getAttendance());
        return dto;
    }
}
