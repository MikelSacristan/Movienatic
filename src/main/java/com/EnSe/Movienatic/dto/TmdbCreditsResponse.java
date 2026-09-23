package com.EnSe.Movienatic.dto;

import java.util.List;

public record TmdbCreditsResponse(
        List<TmdbCastDto> cast) {
}