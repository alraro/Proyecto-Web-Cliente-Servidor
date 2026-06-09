package es.grupo8.backend.dto;

import lombok.Data;

/**
 * DTO for assigning a volunteer to a shift.
 * Used by coordinator endpoint POST /api/coordinator/volunteer-shifts.
 */
@Data
public class CoordinatorVolunteerShiftRequestDto {
    private Integer volunteerId;
    private Integer campaignId;
    private Integer storeId;
    private String shiftDay;
    private String startTime;
    private String endTime;
}
