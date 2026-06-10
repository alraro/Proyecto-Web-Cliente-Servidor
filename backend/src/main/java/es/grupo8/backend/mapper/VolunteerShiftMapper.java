/**
 * Mapeador entre la entidad VolunteerShift y su DTO.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 90%
 * - IA Generativa: 10%
 */
package es.grupo8.backend.mapper;

import es.grupo8.backend.dto.VolunteerShiftDTO;
import es.grupo8.backend.entity.VolunteerShift;
import org.springframework.stereotype.Component;

/**
 * Maps {@link VolunteerShift} entities to {@link VolunteerShiftDTO}.
 */
@Component
public class VolunteerShiftMapper extends MapperDTO<VolunteerShiftDTO, VolunteerShift> {

    @Override
    public VolunteerShiftDTO toDTO(VolunteerShift vs) {
        if (vs == null) return null;
        return new VolunteerShiftDTO(
                vs.getIdVolunteer().getId(),
                vs.getIdVolunteer().getName(),
                vs.getIdVolunteer().getPhone(),
                vs.getId().getShiftDay(),
                vs.getId().getStartTime(),
                vs.getEndTime(),
                vs.getAttendance()
        );
    }
}
