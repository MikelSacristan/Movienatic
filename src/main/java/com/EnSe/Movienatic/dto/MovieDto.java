package com.EnSe.Movienatic.dto;

import com.EnSe.Movienatic.model.CastMember;
import java.util.List;

// DTO para la transferencia de datos de películas, incluyendo información de casting y reseñas
public record MovieDto(
        Long id,
        String title,
        String description,
        String genre,
        int releaseYear,
        List<CastMember> casting,
        String posterURL,
        Double averageRating,
        Integer numberOfRatings,
        List<ReviewDto> reviews) {

}