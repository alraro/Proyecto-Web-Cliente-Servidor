package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Embeddable
public class VolunteerShiftId implements Serializable {
    private static final long serialVersionUID = 9195324904145402746L;
    @Column(name = "id_volunteer", nullable = false)
    private Integer idVolunteer;

    @Column(name = "id_campaign", nullable = false)
    private Integer idCampaign;

    @Column(name = "id_store", nullable = false)
    private Integer idStore;

    @Column(name = "shift_day", nullable = false)
    private LocalDate shiftDay;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    public Integer getIdVolunteer() {
        return idVolunteer;
    }

    public void setIdVolunteer(Integer idVolunteer) {
        this.idVolunteer = idVolunteer;
    }

    public Integer getIdCampaign() {
        return idCampaign;
    }

    public void setIdCampaign(Integer idCampaign) {
        this.idCampaign = idCampaign;
    }

    public Integer getIdStore() {
        return idStore;
    }

    public void setIdStore(Integer idStore) {
        this.idStore = idStore;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VolunteerShiftId entity = (VolunteerShiftId) o;
        return Objects.equals(this.idVolunteer, entity.idVolunteer) &&
                Objects.equals(this.idCampaign, entity.idCampaign) &&
                Objects.equals(this.idStore, entity.idStore) &&
                Objects.equals(this.shiftDay, entity.shiftDay) &&
                Objects.equals(this.startTime, entity.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVolunteer, idCampaign, idStore, shiftDay, startTime);
    }
}