/**
 * Autores:
 * - Alejandro Calvo Aguilar: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;


@Data
public class ShiftCalendarItemDto {
    private Integer shiftId;
    private String startTime;
    private String endTime;
    private Integer volunteersNeeded;
    private Integer volunteersAssigned;
    private String observations;
}
