package com.EnSe.Movienatic.dto;

import java.util.List;

// record DTO para representar la respuesta de géneros de la API de TMDB, que contiene una lista de géneros
public record TmdbGenreResponse(
                List<TmdbGenreDto> genres) {
}