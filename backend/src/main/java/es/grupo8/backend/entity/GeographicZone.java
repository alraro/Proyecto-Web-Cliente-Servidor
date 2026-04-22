package es.grupo8.backend.entity;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "geographic_zones", uniqueConstraints = {@UniqueConstraint(name = "geographic_zones_name_key",
        columnNames = {"name"})})
public class GeographicZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zone", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "idZone")
    private Set<Locality> localities = new LinkedHashSet<>();

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

    public Set<Locality> getLocalities() {
        return localities;
    }

    public void setLocalities(Set<Locality> localities) {
        this.localities = localities;
    }

}