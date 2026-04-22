package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CaptainId implements Serializable {
    private static final long serialVersionUID = 6152896981286655254L;
    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @Column(name = "id_campaign", nullable = false)
    private Integer idCampaign;

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Integer getIdCampaign() {
        return idCampaign;
    }

    public void setIdCampaign(Integer idCampaign) {
        this.idCampaign = idCampaign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaptainId entity = (CaptainId) o;
        return Objects.equals(this.idUser, entity.idUser) &&
                Objects.equals(this.idCampaign, entity.idCampaign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser, idCampaign);
    }
}