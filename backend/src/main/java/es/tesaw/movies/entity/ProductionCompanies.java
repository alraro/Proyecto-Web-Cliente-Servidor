package es.tesaw.movies.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 *
 * @author guzman
 */
@Entity
@Data
@Table(name = "PRODUCTION_COMPANIES")
public class ProductionCompanies implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Column(name = "NAME")
    private String name;
    @ManyToMany(mappedBy = "productionCompaniesList")
    private List<Movies> moviesList;


    @Override
    public String toString() {
        return "es.taw.movies.entity.ProductionCompanies[ id=" + id + " ]";
    }
    
}
