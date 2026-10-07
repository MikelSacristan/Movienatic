package com.EnSe.Movienatic.dto;

// DTO para representar un género de película en la respuesta de la API de TMDB
public record TmdbGenreDto(
                Long id,
                String name) {
}