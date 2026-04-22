package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "partner_entity_managers")
public class PartnerEntityManager {
    @Id
    @Column(name = "id_user", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_user", nullable = false)
    private UserEntity userAccounts;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_partner_entity")
    private PartnerEntity idPartnerEntity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserEntity getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(UserEntity userAccounts) {
        this.userAccounts = userAccounts;
    }

    public PartnerEntity getIdPartnerEntity() {
        return idPartnerEntity;
    }

    public void setIdPartnerEntity(PartnerEntity idPartnerEntity) {
        this.idPartnerEntity = idPartnerEntity;
    }

}