package es.tesaw.movies.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author guzman
 */
@Entity
@Data
@Table(name = "MOVIES")
public class Movies implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "ORIGINAL_TITLE")
    private String originalTitle;
    @Column(name = "OVERVIEW")
    private String overview;
    @Column(name = "RELEASE_DATE")
    @Temporal(TemporalType.DATE)
    private Date releaseDate;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "RUNTIME")
    private Double runtime;
    @Column(name = "BUDGET")
    private BigInteger budget;
    @Column(name = "REVENUE")
    private BigInteger revenue;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "TAGLINE")
    private String tagline;
    @Column(name = "ORIGINAL_LANGUAGE")
    private String originalLanguage;
    @Column(name = "POPULARITY")
    private Double popularity;
    @Column(name = "VOTE_AVERAGE")
    private Double voteAverage;
    @Column(name = "VOTE_COUNT")
    private Integer voteCount;
    @Column(name = "HOMEPAGE")
    private String homepage;
    @JoinTable(name = "MOVIE_GENRES", joinColumns = {
        @JoinColumn(name = "MOVIE_ID", referencedColumnName = "ID")}, inverseJoinColumns = {
        @JoinColumn(name = "GENRE_ID", referencedColumnName = "ID")})
    @ManyToMany
    private List<Genres> genresList;
    @JoinTable(name = "MOVIE_LANGUAGES", joinColumns = {
        @JoinColumn(name = "MOVIE_ID", referencedColumnName = "ID")}, inverseJoinColumns = {
        @JoinColumn(name = "LANGUAGE_CODE", referencedColumnName = "ISO_639_1")})
    @ManyToMany
    private List<SpokenLanguages> spokenLanguagesList;
    @JoinTable(name = "MOVIE_PRODUCTION_COMPANIES", joinColumns = {
        @JoinColumn(name = "MOVIE_ID", referencedColumnName = "ID")}, inverseJoinColumns = {
        @JoinColumn(name = "COMPANY_ID", referencedColumnName = "ID")})
    @ManyToMany
    private List<ProductionCompanies> productionCompaniesList;
    @OneToMany(mappedBy = "movieId")
    private List<MovieCast> movieCastList;
    @OneToMany(mappedBy = "movieId")
    private List<MovieCrew> movieCrewList;

    @Override
    public String toString() {
        return "es.taw.movies.entity.Movies[ id=" + id + " ]";
    }
    
}
