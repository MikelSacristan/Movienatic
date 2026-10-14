package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);

    Optional<Movie> findByTitle(String title);

    // Devuelve solo los títulos en una única consulta, para comprobar en
    // memoria cuáles ya están importados sin lanzar una consulta por película
    @Query("select m.title from Movie m")
    List<String> findAllTitles();

    @Query("SELECT m FROM User u JOIN u.pendingMovies m WHERE u.id = :userId")
    List<Movie> findPendingMoviesByUserId(@Param("userId") Long userId);
}