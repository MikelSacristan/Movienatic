package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.exception.MovieNotFoundException;
import com.EnSe.Movienatic.service.MovieService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {

    // Tamaño de página por defecto: 4 columnas x 5 filas = 20 películas
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("")
    public Page<MovieDto> getMovies(@PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return movieService.getMovies(pageable);
    }

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable Long id) {
        MovieDto movie = movieService.getMovieById(id);
        if (movie == null) {
            throw new MovieNotFoundException(HttpStatus.NOT_FOUND, "Película no encontrada: " + id);
        }
        return movie;
    }
}