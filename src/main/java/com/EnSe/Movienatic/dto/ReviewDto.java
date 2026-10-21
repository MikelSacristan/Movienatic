package com.EnSe.Movienatic.dto;

import com.EnSe.Movienatic.dto.MovieDto.ReviewView;
import com.fasterxml.jackson.annotation.JsonView;

// DTO para representar una reseña de película
public record ReviewDto(
        Long id,
        @JsonView(ReviewView.class) String content,
        @JsonView(ReviewView.class) Integer rating,
        @JsonView(ReviewView.class) Integer likes,
        @JsonView(ReviewView.class) Integer dislikes,
        @JsonView(ReviewView.class) Long timestamp) {

}