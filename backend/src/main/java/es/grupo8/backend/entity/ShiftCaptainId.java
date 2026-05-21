package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ShiftCaptainId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_shift", nullable = false)
    private Integer idShift;

    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    public Integer getIdShift() { return idShift; }
    public void setIdShift(Integer idShift) { this.idShift = idShift; }

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShiftCaptainId)) return false;
        ShiftCaptainId that = (ShiftCaptainId) o;
        return Objects.equals(idShift, that.idShift) && Objects.equals(idUser, that.idUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idShift, idUser);
    }
}
