package com.EnSe.Movienatic.dto;

import java.util.List;

// DTO para representar una lista de películas de un usuario
public record MovieListDto(
        Long id,
        String name,
        String description,
        int movieCount,
        List<String> previewPosters) {
}
