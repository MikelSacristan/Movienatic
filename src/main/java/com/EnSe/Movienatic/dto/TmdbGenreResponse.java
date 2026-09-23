package com.EnSe.Movienatic.dto;

import java.util.List;

public record TmdbGenreResponse(
        List<TmdbGenreDto> genres) {
}