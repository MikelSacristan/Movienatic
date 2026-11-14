package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.MovieListLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieListLikeRepository extends JpaRepository<MovieListLike, Long> {

    Optional<MovieListLike> findByUser_IdAndMovieList_Id(Long userId, Long movieListId);

    long countByMovieList_Id(Long movieListId);

    void deleteByMovieList_Id(Long movieListId);

    List<MovieListLike> findByUser_Id(Long userId);
}
