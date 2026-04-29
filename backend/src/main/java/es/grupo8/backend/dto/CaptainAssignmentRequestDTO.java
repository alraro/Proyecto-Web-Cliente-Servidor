package es.grupo8.backend.dto;

public class CaptainAssignmentRequestDTO {
    private Integer userId;
    private Integer campaignId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }
}