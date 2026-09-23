package com.EnSe.Movienatic.dto;

import java.util.List;

// Record que representa la respuesta de la API de TMDB para películas populares, incluyendo la página actual y una lista de resultados de películas
// Jackson se encargará de mapear automáticamente los campos JSON a los atributos del record
public record TmdbResponse(
                int page,
                List<TmdbMovieDto> results) {
}