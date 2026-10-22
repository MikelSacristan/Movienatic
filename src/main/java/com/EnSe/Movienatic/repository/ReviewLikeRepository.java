package com.EnSe.Movienatic.repository;

import com.EnSe.Movienatic.model.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    @Query("select count(r) from ReviewLike r where r.review.id = :reviewId and r.isLike = true")
    Long countLikesByReviewId(Long reviewId);

    @Query("select count(r) from ReviewLike r where r.review.id = :reviewId and r.isLike = false")
    Long countDislikesByReviewId(Long reviewId);

    @Query("select r from ReviewLike r where r.user.id = :userId and r.review.id = :reviewId")
    Optional<ReviewLike> findByUserIdAndReviewId(Long userId, Long reviewId);

    @Modifying
    @Query("delete from ReviewLike r where r.user.id = :userId and r.review.id = :reviewId")
    void deleteByUserIdAndReviewId(Long userId, Long reviewId);
}
