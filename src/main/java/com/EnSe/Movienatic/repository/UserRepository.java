package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByUsernameOrEmail(String username, String email);
    @Query("SELECT m FROM User u JOIN u.pendingMovies m WHERE u.id = :userId")
    List<Movie> findPendingMovies(@Param("userId") Long userId);
}