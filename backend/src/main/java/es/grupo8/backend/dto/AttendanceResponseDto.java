/**
 * Autores:
 * - Alejandro Calvo Aguilar: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class AttendanceResponseDto {
    private String message;
    private Integer shiftId;
    private Integer volunteerId;
    private Boolean attendance;
}
