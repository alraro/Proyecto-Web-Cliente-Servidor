package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO representing a day entry in the calendar, containing its list of shifts.
 */
@Data
public class ShiftCalendarDayDto {
    private String date;
    private List<ShiftCalendarItemDto> shifts;
}
