package com.EnSe.Movienatic.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<MovieList> movieLists;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @ManyToMany
    @JoinTable(name = "user_favorite_movies", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private Set<Movie> favoriteMovies = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_pending_movies", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private Set<Movie> pendingMovies = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_follows", joinColumns = @JoinColumn(name = "follower_id"), // El que sigue (yo)
            inverseJoinColumns = @JoinColumn(name = "following_id") // Al que sigo
    )
    private Set<User> following = new HashSet<>();

    // Usuarios que me siguen a mí (lado inverso)
    @ManyToMany(mappedBy = "following")
    private Set<User> followers = new HashSet<>();

    private String photoURL;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public List<MovieList> getMovieLists() {
        return movieLists;
    }

    public void setMovieLists(List<MovieList> movieLists) {
        this.movieLists = movieLists;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Movie> getFavoriteMovies() {
        return favoriteMovies;
    }

    public void setFavoriteMovies(Set<Movie> favoriteMovies) {
        this.favoriteMovies = favoriteMovies;
    }

    public Set<Movie> getPendingMovies() {
        return pendingMovies;
    }

    public void setPendingMovies(Set<Movie> pendingMovies) {
        this.pendingMovies = pendingMovies;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }
}
