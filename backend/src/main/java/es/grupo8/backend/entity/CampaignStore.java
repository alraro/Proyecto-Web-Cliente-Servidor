package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "campaign_stores")
public class CampaignStore {
    @EmbeddedId
    private CampaignStoreId id;

    @MapsId("idCampaign")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_campaign", nullable = false)
    private Campaign idCampaign;

    @MapsId("idStore")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_store", nullable = false)
    private Store idStore;

    @OneToMany(mappedBy = "campaignStores")
    private Set<VolunteerShift> volunteerShifts = new LinkedHashSet<>();

    public CampaignStoreId getId() {
        return id;
    }

    public void setId(CampaignStoreId id) {
        this.id = id;
    }

    public Campaign getIdCampaign() {
        return idCampaign;
    }

    public void setIdCampaign(Campaign idCampaign) {
        this.idCampaign = idCampaign;
    }

    public Store getIdStore() {
        return idStore;
    }

    public void setIdStore(Store idStore) {
        this.idStore = idStore;
    }

    public Set<VolunteerShift> getVolunteerShifts() {
        return volunteerShifts;
    }

    public void setVolunteerShifts(Set<VolunteerShift> volunteerShifts) {
        this.volunteerShifts = volunteerShifts;
    }

}