package com.EnSe.Movienatic.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.EnSe.Movienatic.model.Review;
import com.EnSe.Movienatic.model.ReviewLike;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    @Query("select r from ReviewLike r where r.user.id = :userId and r.review.id = :reviewId")
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    @Modifying
    @Query("delete from ReviewLike r where r.user.id = :userId and r.review.id = :reviewId")
    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    @Query("select count(r) from ReviewLike r where r.review = :review and r.isLike = :b")
    Integer countByReviewAndIsLike(Review review, boolean b);
}
