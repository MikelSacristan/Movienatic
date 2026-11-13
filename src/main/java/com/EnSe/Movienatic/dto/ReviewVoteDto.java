package com.EnSe.Movienatic.dto;

// Voto (like/dislike) de un usuario sobre una reseña.
public record ReviewVoteDto(
        Long reviewId,
        Boolean isLike) {
}
