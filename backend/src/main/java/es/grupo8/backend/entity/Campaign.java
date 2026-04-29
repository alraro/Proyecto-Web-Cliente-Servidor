package es.grupo8.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "campaigns")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_campaign", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "id_type")
    private CampaignType idType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
        name = "campaign_stores",
        joinColumns = @JoinColumn(name = "id_campaign"),
        inverseJoinColumns = @JoinColumn(name = "id_store")
    )
    private Set<Store> stores = new LinkedHashSet<>();

    @ManyToMany
    private Set<UserEntity> captains = new LinkedHashSet<>();

    @ManyToMany
    private Set<UserEntity> coordinators = new LinkedHashSet<>();

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

    public CampaignType getIdType() {
        return idType;
    }

    public void setIdType(CampaignType idType) {
        this.idType = idType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Set<Store> getStores() {
        return stores;
    }

    public void setStores(Set<Store> stores) {
        this.stores = stores;
    }

    public Set<UserEntity> getCaptains() {
        return captains;
    }

    public void setCaptains(Set<UserEntity> captains) {
        this.captains = captains;
    }

    public Set<UserEntity> getCoordinators() {
        return coordinators;
    }

    public void setCoordinators(Set<UserEntity> coordinators) {
        this.coordinators = coordinators;
    }

}