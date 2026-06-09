package es.grupo8.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShiftCaptainsResponseDto {
    private Integer shiftId;
    private List<CaptainAssignmentDto> captains;
}
