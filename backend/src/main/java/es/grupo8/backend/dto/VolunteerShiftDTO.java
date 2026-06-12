/**
 * DTO para transferir turnos de voluntarios.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Volunteer assignment to a shift, including attendance status.
 *
 * @param volunteerId   identifier of the volunteer
 * @param volunteerName full name of the volunteer
 * @param phone         contact phone of the volunteer
 * @param shiftDay      date of the shift
 * @param startTime     shift start time
 * @param endTime       shift end time
 * @param attendance    whether the volunteer attended
 */
public record VolunteerShiftDTO(
        Integer volunteerId,
        String volunteerName,
        String phone,
        LocalDate shiftDay,
        LocalTime startTime,
        LocalTime endTime,
        Boolean attendance
) {}
