package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.Movie;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Hereda de JpaRepository para operaciones CRUD y de JpaSpecificationExecutor para consultas con filtros dinámicos
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    boolean existsByTitle(String title);

    Optional<Movie> findByTitle(String title);

    // Devuelve solo los títulos en una única consulta, para comprobar en
    // memoria cuáles ya están importados sin lanzar una consulta por película
    @Query("select m.title from Movie m")
    List<String> findAllTitles();

    @Query("SELECT m FROM User u JOIN u.pendingMovies m WHERE u.id = :userId")
    List<Movie> findPendingMoviesByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM User u JOIN u.pendingMovies m WHERE u.id = :userId ORDER BY m.title ASC, m.id ASC")
    Page<Movie> findPendingMoviesPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM User u JOIN u.favouriteMovies m WHERE u.id = :userId ORDER BY m.title ASC, m.id ASC")
    Page<Movie> findFavouriteMoviesPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM MovieList l JOIN l.movies m WHERE l.id = :listId AND l.user.id = :userId ORDER BY m.title ASC, m.id ASC")
    Page<Movie> findMoviesByListIdAndUserId(@Param("listId") Long listId,
            @Param("userId") Long userId,
            Pageable pageable);

    @Query("SELECT COUNT(m) > 0 FROM User u JOIN u.favouriteMovies m WHERE u.id = :userId AND m.id = :movieId")
    boolean existsFavouriteMovie(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Query("SELECT COUNT(m) > 0 FROM User u JOIN u.pendingMovies m WHERE u.id = :userId AND m.id = :movieId")
    boolean existsPendingMovie(@Param("userId") Long userId, @Param("movieId") Long movieId);

    @Query("SELECT COUNT(m) FROM MovieList l JOIN l.movies m WHERE l.id = :listId")
    long countMoviesByListId(@Param("listId") Long listId);

    @Query("SELECT m.posterURL FROM MovieList l JOIN l.movies m "
            + "WHERE l.id = :listId AND m.posterURL IS NOT NULL ORDER BY m.title ASC")
    List<String> findPosterUrlsByListId(@Param("listId") Long listId, Pageable pageable);
}