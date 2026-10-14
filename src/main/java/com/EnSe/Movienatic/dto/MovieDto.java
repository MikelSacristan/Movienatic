package com.EnSe.Movienatic.dto;

import com.EnSe.Movienatic.model.CastMember;
import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

// DTO para la transferencia de datos de películas, incluyendo información de casting y reseñas
public record MovieDto(
        @JsonView(SummaryView.class)
        Long id,
        @JsonView(SummaryView.class)
        String title,
        @JsonView(DetailView.class)
        String description,
        @JsonView(DetailView.class)
        String genre,
        @JsonView(DetailView.class)
        int releaseYear,
        @JsonView(DetailView.class)
        List<CastMember> casting,
        @JsonView(DetailView.class)
        String posterURL,
        @JsonView(DetailView.class)
        Double averageRating,
        @JsonView(DetailView.class)
        Integer numberOfRatings,
        @JsonView(DetailView.class)
        List<ReviewDto> reviews) {
        
        public interface SummaryView{}
        public interface DetailView extends SummaryView{}

}