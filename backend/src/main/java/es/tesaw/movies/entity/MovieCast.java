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
@Table(name = "MOVIE_CAST")
public class MovieCast implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Column(name = "CHARACTER_NAME")
    private String characterName;
    @Column(name = "CAST_ORDER")
    private Integer castOrder;
    @JoinColumn(name = "MOVIE_ID", referencedColumnName = "ID")
    @ManyToOne
    private Movies movieId;
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "ID")
    @ManyToOne
    private People personId;

    @Override
    public String toString() {
        return "es.taw.movies.entity.MovieCast[ id=" + id + " ]";
    }
    
}
