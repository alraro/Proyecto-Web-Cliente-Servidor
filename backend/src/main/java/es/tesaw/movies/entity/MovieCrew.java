package es.tesaw.movies.entity;

import lombok.Data;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 *
 * @author guzman
 */
@Entity
@Data
@Table(name = "MOVIE_CREW")
public class MovieCrew implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "DEPARTMENT")
    private String department;
    @Column(name = "JOB")
    private String job;
    @JoinColumn(name = "MOVIE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Movies movieId;
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "ID")
    @ManyToOne
    private People personId;

    @Override
    public String toString() {
        return "es.taw.movies.entity.MovieCrew[ id=" + id + " ]";
    }
    
}
