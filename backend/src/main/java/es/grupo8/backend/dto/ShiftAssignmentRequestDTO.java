package es.grupo8.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShiftAssignmentRequestDTO {
    private Integer volunteerId;
    private Integer campaignId;
    private Integer storeId;
    private LocalDate shiftDay;
    private LocalTime startTime;
    private LocalTime endTime;

    public Integer getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(Integer volunteerId) {
        this.volunteerId = volunteerId;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public LocalDate getShiftDay() {
        return shiftDay;
    }

    public void setShiftDay(LocalDate shiftDay) {
        this.shiftDay = shiftDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}