package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "localities")
public class Locality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_locality", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_zone")
    private GeographicZone idZone;

    @OneToMany(mappedBy = "idLocality")
    private Set<District> districts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "idLocality")
    private Set<PostalCode> postalCodes = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GeographicZone getIdZone() {
        return idZone;
    }

    public void setIdZone(GeographicZone idZone) {
        this.idZone = idZone;
    }

    public Set<District> getDistricts() {
        return districts;
    }

    public void setDistricts(Set<District> districts) {
        this.districts = districts;
    }

    public Set<PostalCode> getPostalCodes() {
        return postalCodes;
    }

    public void setPostalCodes(Set<PostalCode> postalCodes) {
        this.postalCodes = postalCodes;
    }

}