package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;

@Entity
@Table(name = "volunteer_shifts")
public class VolunteerShift {
    @EmbeddedId
    private VolunteerShiftId id;

    @MapsId("idVolunteer")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_volunteer", nullable = false)
    private Volunteer idVolunteer;

    @MapsId("id")
    @JoinColumns({
            @JoinColumn(name = "id_campaign",
                    referencedColumnName = "id_campaign",
                    nullable = false),
            @JoinColumn(name = "id_store",
                    referencedColumnName = "id_store",
                    nullable = false)})
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CampaignStore campaignStores;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ColumnDefault("false")
    @Column(name = "attendance")
    private Boolean attendance;

    @Column(name = "notes", length = Integer.MAX_VALUE)
    private String notes;

    public VolunteerShiftId getId() {
        return id;
    }

    public void setId(VolunteerShiftId id) {
        this.id = id;
    }

    public Volunteer getIdVolunteer() {
        return idVolunteer;
    }

    public void setIdVolunteer(Volunteer idVolunteer) {
        this.idVolunteer = idVolunteer;
    }

    public CampaignStore getCampaignStores() {
        return campaignStores;
    }

    public void setCampaignStores(CampaignStore campaignStores) {
        this.campaignStores = campaignStores;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getAttendance() {
        return attendance;
    }

    public void setAttendance(Boolean attendance) {
        this.attendance = attendance;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}