package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.Movie;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    boolean existsByTitle(String title);

    Optional<Movie> findByTitle(String title);
}