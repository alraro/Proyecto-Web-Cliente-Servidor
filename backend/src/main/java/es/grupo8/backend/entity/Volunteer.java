package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "volunteers")
public class Volunteer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_volunteer", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", length = Integer.MAX_VALUE)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_partner_entity")
    private PartnerEntity idPartnerEntity;

    @OneToMany(mappedBy = "idVolunteer")
    private Set<VolunteerShift> volunteerShifts = new LinkedHashSet<>();

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public PartnerEntity getIdPartnerEntity() {
        return idPartnerEntity;
    }

    public void setIdPartnerEntity(PartnerEntity idPartnerEntity) {
        this.idPartnerEntity = idPartnerEntity;
    }

    public Set<VolunteerShift> getVolunteerShifts() {
        return volunteerShifts;
    }

    public void setVolunteerShifts(Set<VolunteerShift> volunteerShifts) {
        this.volunteerShifts = volunteerShifts;
    }

}