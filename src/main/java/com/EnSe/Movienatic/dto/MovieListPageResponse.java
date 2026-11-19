package com.EnSe.Movienatic.dto;

import java.util.List;

// Respuesta paginada de listas de películas (portada y página Social).
public record MovieListPageResponse(
        List<MovieListDto> content,
        int totalPages,
        long totalElements,
        int number,
        int size) {
}
