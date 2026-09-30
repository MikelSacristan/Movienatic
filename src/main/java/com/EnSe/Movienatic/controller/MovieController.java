package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/{id}")
    public MovieDto getMovieById(@PathVariable Long id) {
        MovieDto movie = movieService.getMovieById(id);
        if (movie == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Película no encontrada: " + id);
        }
        return movie;
    }
}