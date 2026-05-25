package es.grupo8.backend.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class CampaignSummaryDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private long storesInCampaign;
    private boolean active;
}