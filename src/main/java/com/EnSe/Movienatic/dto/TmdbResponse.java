package com.EnSe.Movienatic.dto;

import java.util.List;

// record DTO para representar la respuesta de películas de la API de TMDB, que contiene una lista de películas y la página actual
public record TmdbResponse(
        int page,
        List<TmdbMovieDto> results) {
}