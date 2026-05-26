package es.grupo8.backend.dto;

import lombok.Data;


@Data
public class ShiftRequestDto {
    private Integer campaignId;
    private Integer storeId;
    private String day;
    private String startTime;
    private String endTime;
    private Integer volunteersNeeded;
    private String location;
    private String observations;
}
