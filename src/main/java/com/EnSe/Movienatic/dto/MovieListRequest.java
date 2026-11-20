package com.EnSe.Movienatic.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MovieListRequest {
    private String name;
    private String description;
    // Si se envía, reemplaza el contenido de la lista por estas películas.
    // En la creación permite crear la lista y sus películas en una sola petición.
    private List<Long> movieIds;
}
