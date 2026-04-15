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
@Table(name = "SPOKEN_LANGUAGES")
public class SpokenLanguages implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ISO_639_1", nullable = false)
    private String iso6391;
    @Column(name = "NAME")
    private String name;
    @ManyToMany(mappedBy = "spokenLanguagesList")
    private List<Movies> moviesList;

    @Override
    public String toString() {
        return "es.taw.movies.entity.SpokenLanguages[ iso6391=" + iso6391 + " ]";
    }
    
}
