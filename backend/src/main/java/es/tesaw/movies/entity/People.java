package es.tesaw.movies.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author guzman
 */
@Entity
@Data
@Table(name = "PEOPLE")
public class People implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "GENDER")
    private Integer gender;
    @OneToMany(mappedBy = "personId")
    private List<MovieCast> movieCastList;
    @OneToMany(mappedBy = "personId")
    private List<MovieCrew> movieCrewList;

    @Override
    public String toString() {
        return "es.taw.movies.entity.People[ id=" + id + " ]";
    }
    
}
