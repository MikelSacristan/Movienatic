package com.EnSe.Movienatic.dto;

import java.util.List;

// DTO para representar la respuesta de créditos de la API de TMDB, que contiene una lista de actores
public record TmdbCreditsResponse(
                List<TmdbCastDto> cast) {
}