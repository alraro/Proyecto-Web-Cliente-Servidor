package es.grupo8.backend.dto;

import lombok.Data;

@Data
public class CaptainAssignResponseDto {
    private String message;
    private Integer shiftId;
    private Integer userId;
    private String userName;
}
