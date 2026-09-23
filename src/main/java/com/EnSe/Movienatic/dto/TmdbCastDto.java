package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCastDto(
        String name,
        String character,
        @JsonProperty("cast_id") Long castId,
        @JsonProperty("order") Integer order) {
}