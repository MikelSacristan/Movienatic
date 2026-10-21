package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.exception.MovieNotFoundException;
import com.EnSe.Movienatic.model.Movie;
import com.EnSe.Movienatic.service.MovieService;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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

    // Paginación offset: ?page=0&size=20 (permite saltar a cualquier página)
    @GetMapping("")
    public Page<MovieDto> getMovies(@PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return movieService.getMovies(pageable);
    }

    // Solo consulta (GET)

    @GetMapping("/{movieId}")
    @JsonView(MovieDto.SummaryView.class)
    public ResponseEntity<?> getMovieById(@PathVariable("movieId") Long movieId) {
        MovieDto movieDto = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieDto);
    }

    @GetMapping("/{movieId}/detail")
    @JsonView(MovieDto.DetailView.class)
    public ResponseEntity<?> getMovieByIdAll(@PathVariable("movieId") Long movieId) {
        MovieDto movieDto = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieDto);
    }

    @GetMapping("/{movieId}/casting")
    @JsonView(MovieDto.CastingView.class)
    public ResponseEntity<?> getCasting(@PathVariable("movieId") Long movieId) {
        MovieDto movieDto = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieDto);
    }
    @GetMapping("/{movieId}/review")
    @JsonView(MovieDto.ReviewView.class)
    public ResponseEntity<?> getReviews(@PathVariable("movieId") Long movieId) {
        MovieDto movieDto = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieDto);
    }
}