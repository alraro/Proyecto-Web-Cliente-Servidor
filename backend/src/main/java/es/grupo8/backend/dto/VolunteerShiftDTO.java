/**
 * DTO para transferir turnos de voluntarios.
 *
 * Autores:
 * - Fernando Luis Pinilla Molina: 100%
 */
package es.grupo8.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class VolunteerShiftDTO {
    private Integer volunteerId;
    private String volunteerName;
    private String phone;
    private LocalDate shiftDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean attendance;
}
