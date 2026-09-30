package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmdbCastDto(
                String name,
                String character,
                @JsonProperty("cast_id") Long castId, // por ahora no se utiliza
                @JsonProperty("order") Integer order) { // orden de aparición en los créditos, para ordenar los actores
                                                        // por relevancia
}