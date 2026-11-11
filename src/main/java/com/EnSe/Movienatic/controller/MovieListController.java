package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.PendingRequest;
import com.EnSe.Movienatic.dto.FavouriteRequest;
import com.EnSe.Movienatic.dto.MovieListRequest;
import com.EnSe.Movienatic.dto.MovieRequest;
import com.EnSe.Movienatic.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}")
public class MovieListController {

    private final UserService userService;

    public MovieListController(UserService userService) {
        this.userService = userService;
    }

    // Películas pendientes (GET, POST, DELETE)
    @GetMapping("/pending")
    @JsonView(MovieDto.SummaryView.class)
    public ResponseEntity<?> getPendingMovies(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getPendingMovies(userId));
    }

    @PostMapping("/pending")
    public ResponseEntity<?> addPending(@PathVariable("userId") Long userId,
            @RequestBody PendingRequest movieRequest) {
        userService.addPending(userId, movieRequest.movieId());
        return ResponseEntity.ok().body("Película añadida a pendientes correctamente");
    }

    @DeleteMapping("/pending/{movieId}")
    public ResponseEntity<?> removePending(@PathVariable("userId") Long userId,
            @PathVariable("movieId") Long movieId) {
        userService.removePending(userId, movieId);
        return ResponseEntity.ok().body("Película eliminada de pendientes correctamente");
    }

    // Favoritas (GET, POST, DELETE)
    @GetMapping("/favourites")
    @JsonView(MovieDto.SummaryView.class)
    public ResponseEntity<?> getFavourites(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getFavourites(userId));
    }

    @PostMapping("/favourites")
    public ResponseEntity<?> addFavourite(@PathVariable("userId") Long userId,
            @RequestBody FavouriteRequest movieRequest) {
        userService.addFavourite(userId, movieRequest.movieId());
        return ResponseEntity.ok().body("Película añadida a favoritas correctamente");
    }

    @DeleteMapping("/favourites/{movieId}")
    public ResponseEntity<?> removeFavourite(@PathVariable("userId") Long userId,
            @PathVariable("movieId") Long movieId) {
        userService.removeFavourite(userId, movieId);
        return ResponseEntity.ok().body("Película eliminada de favoritas correctamente");
    }

    // Listas creadas por el usuario (GET, POST, DELETE)
    @GetMapping("/lists")
    public ResponseEntity<?> getLists(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getLists(userId));
    }

    @PostMapping("/lists")
    public ResponseEntity<?> addList(@PathVariable("userId") Long userId, @RequestBody MovieListRequest request) {
        userService.addList(userId, request.getName(), request.getDescription());
        return ResponseEntity.ok().body("Lista de películas creada correctamente");
    }

    @DeleteMapping("/lists/{listId}")
    public ResponseEntity<?> removeList(@PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId) {
        userService.removeList(userId, listId);
        return ResponseEntity.ok().body("Lista de películas borrada correctamente");
    }

    @GetMapping("/lists/{listId}")
    public ResponseEntity<?> getListById(@PathVariable("userId") Long userId,
            @PathVariable("listId") Long listId) {
        return ResponseEntity.ok(userService.getListById(userId, listId));
    }

    @PostMapping("/lists/{listId}")
    public ResponseEntity<?> addMovieToList(@PathVariable("userId") Long userId, @PathVariable("listId") Long listId,
            @RequestBody MovieRequest request) {
        userService.addMovieToList(userId, listId, request.getId());
        return ResponseEntity.ok().body("Película añadida a lista correctamente");
    }

    @PatchMapping("/lists/{listId}")
    public ResponseEntity<?> removeMovieToList(@PathVariable("userId") Long userId, @PathVariable("listId") Long listId,
            @RequestBody MovieRequest request) {
        userService.removeMovieToList(userId, listId, request.getId());
        return ResponseEntity.ok().body("Película eliminada de la lista correctamente");
    }
}
