package es.grupo8.backend.dto;

import lombok.Data;

/**
 * DTO representing a single shift slot in the visual calendar view.
 */
@Data
public class ShiftCalendarItemDto {
    private Integer shiftId;
    private String startTime;
    private String endTime;
    private Integer volunteersNeeded;
    private Integer volunteersAssigned;
    private String observations;
}
