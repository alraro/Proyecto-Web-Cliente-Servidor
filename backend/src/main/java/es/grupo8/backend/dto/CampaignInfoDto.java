package es.grupo8.backend.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CampaignInfoDto {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private long volunteerCount;
}
