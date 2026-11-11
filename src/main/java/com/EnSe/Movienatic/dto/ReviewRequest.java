package com.EnSe.Movienatic.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private Long movieId;
    private String content;
    // el rating de las reseñas solo podrán ir de 0.5 en 0.5 hasta 5.0

    @DecimalMin("0.5")
    @DecimalMax("5.0")
    private Double rating;

    public ReviewRequest() {
    }

    public ReviewRequest(Long movieId, String content, Double rating) {
        this.movieId = movieId;
        this.content = content;
        this.rating = rating;
    }
}
