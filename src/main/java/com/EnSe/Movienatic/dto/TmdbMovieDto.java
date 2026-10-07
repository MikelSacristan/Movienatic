package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// DTO para representar una película en la respuesta de la API de TMDB
public record TmdbMovieDto(
                Long id,
                String title,
                String overview,
                @JsonProperty("release_date") String releaseDate,
                @JsonProperty("poster_path") String posterPath,
                @JsonProperty("vote_average") Double voteAverage,
                @JsonProperty("vote_count") Integer voteCount,
                @JsonProperty("genre_ids") List<Integer> genreIds) {
}