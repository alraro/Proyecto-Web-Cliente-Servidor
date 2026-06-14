/**
 * Autores:
 * - Alejandro Calvo Aguilar: 100%
 */
package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;


@Data
public class ShiftCalendarDayDto {
    private String date;
    private List<ShiftCalendarItemDto> shifts;
}
