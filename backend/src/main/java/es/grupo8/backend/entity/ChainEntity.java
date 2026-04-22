package es.grupo8.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chains")
public class ChainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chain")
    private Integer idChain;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "participation", nullable = false)
    private Boolean participation = false;


    public Integer getIdChain() {
        return idChain;
    }

    public void setIdChain(Integer idChain) {
        this.idChain = idChain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getParticipation() {
        return participation;
    }

    public void setParticipation(Boolean participation) {
        this.participation = participation;
    }
}