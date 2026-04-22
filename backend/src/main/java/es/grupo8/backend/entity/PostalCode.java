package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "postal_codes")
public class PostalCode {
    @Id
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_locality", nullable = false)
    private Locality idLocality;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_district")
    private District idDistrict;

    @OneToMany(mappedBy = "postalCode")
    private Set<Store> stores = new LinkedHashSet<>();

    @OneToMany(mappedBy = "postalCode")
    private Set<UserEntity> userAccounts = new LinkedHashSet<>();

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Locality getIdLocality() {
        return idLocality;
    }

    public void setIdLocality(Locality idLocality) {
        this.idLocality = idLocality;
    }

    public District getIdDistrict() {
        return idDistrict;
    }

    public void setIdDistrict(District idDistrict) {
        this.idDistrict = idDistrict;
    }

    public Set<Store> getStores() {
        return stores;
    }

    public void setStores(Set<Store> stores) {
        this.stores = stores;
    }

    public Set<UserEntity> getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(Set<UserEntity> userAccounts) {
        this.userAccounts = userAccounts;
    }

}