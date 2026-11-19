package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.MovieList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface MovieListRepository extends JpaRepository<MovieList, Long>{
    Optional<MovieList> findByUser_IdAndId(Long userId, Long id);

    // Listas ordenadas por número de likes (las más populares primero).
    @Query(value = "SELECT l FROM MovieList l LEFT JOIN MovieListLike ml ON ml.movieList = l "
            + "GROUP BY l.id ORDER BY COUNT(ml) DESC, l.id ASC",
            countQuery = "SELECT COUNT(l) FROM MovieList l")
    Page<MovieList> findAllOrderByLikes(Pageable pageable);
}
