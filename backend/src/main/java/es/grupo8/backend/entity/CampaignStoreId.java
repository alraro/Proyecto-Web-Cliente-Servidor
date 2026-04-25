package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CampaignStoreId implements Serializable {
    private static final long serialVersionUID = 2668185612107015243L;
    @Column(name = "id_campaign", nullable = false)
    private Integer idCampaign;

    @Column(name = "id_store", nullable = false)
    private Integer idStore;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CampaignStoreId entity = (CampaignStoreId) o;
        return Objects.equals(this.idCampaign, entity.idCampaign) &&
                Objects.equals(this.idStore, entity.idStore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCampaign, idStore);
    }
}