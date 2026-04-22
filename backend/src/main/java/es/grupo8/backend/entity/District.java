package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_district", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_locality")
    private Locality idLocality;

    @OneToMany(mappedBy = "idDistrict")
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

    public Locality getIdLocality() {
        return idLocality;
    }

    public void setIdLocality(Locality idLocality) {
        this.idLocality = idLocality;
    }

    public Set<PostalCode> getPostalCodes() {
        return postalCodes;
    }

    public void setPostalCodes(Set<PostalCode> postalCodes) {
        this.postalCodes = postalCodes;
    }

}