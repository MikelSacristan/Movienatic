package com.EnSe.Movienatic.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movies", indexes = {
        @jakarta.persistence.Index(name = "idx_title", columnList = "title"),
        @jakarta.persistence.Index(name = "idx_genre", columnList = "genre"),
        @jakarta.persistence.Index(name = "idx_release_year", columnList = "releaseYear")
})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 500)
    private String genre;

    @Column(nullable = false)
    private int releaseYear;

    @ElementCollection
    @CollectionTable(name = "movie_casting", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "position")
    private List<CastMember> casting = new ArrayList<>();

    @Column(nullable = true)
    private String posterURL;
    @DecimalMin("0.5")
    @DecimalMax("5.0")
    private Double averageRating;

    @Column(nullable = true)
    private Integer numberOfRatings;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Review> reviews;
}