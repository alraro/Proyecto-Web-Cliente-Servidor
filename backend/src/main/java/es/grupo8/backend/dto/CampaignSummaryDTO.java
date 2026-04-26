package es.grupo8.backend.dto;

import java.time.LocalDate;

public class CampaignSummaryDTO {
    private Integer id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private long storesInCampaign;
    private boolean active; // startDate <= hoy <= endDate

    public CampaignSummaryDTO(Integer id, String name, LocalDate startDate, LocalDate endDate, long storesInCampaign) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.storesInCampaign = storesInCampaign;
        LocalDate today = LocalDate.now();
        this.active = !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public Integer getId() { 
        return id; 
    }
    public String getName() { 
        return name; 
    }
    public LocalDate getStartDate() { 
        return startDate; 
    }
    public LocalDate getEndDate() { 
        return endDate; 
    }
    public long getStoresInCampaign() { 
        return storesInCampaign; 
    }
    public boolean isActive() { 
        return active; 
    }
}