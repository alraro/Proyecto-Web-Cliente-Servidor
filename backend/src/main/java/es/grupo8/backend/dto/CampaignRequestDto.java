package es.grupo8.backend.dto;

import lombok.Data;
import java.time.LocalDate;

// DTO for creating or updating a campaign.
@Data
public class CampaignRequestDto {
    private String name;
    private Integer typeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
