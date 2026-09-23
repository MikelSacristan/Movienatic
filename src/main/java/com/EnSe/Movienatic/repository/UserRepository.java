package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository {
    Optional<User> findByUsername(String username);
}