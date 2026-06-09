package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShiftVolunteersResponseDto {
    private Integer shiftId;
    private Integer volunteersNeeded;
    private Integer volunteersAssigned;
    private List<VolunteerAssignmentDto> volunteers;
}
