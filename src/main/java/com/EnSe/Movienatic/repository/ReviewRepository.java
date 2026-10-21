package com.EnSe.Movienatic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.EnSe.Movienatic.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
