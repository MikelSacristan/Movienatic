package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.MovieList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MovieListRepository extends JpaRepository<MovieList, Long>{
    Optional<MovieList> findByUserIdAndId(Long userId,Long id);
}
