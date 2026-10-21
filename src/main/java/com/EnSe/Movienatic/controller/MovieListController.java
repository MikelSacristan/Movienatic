package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/lists")
public class MovieListController {

    private final UserService userService;

    public MovieListController(UserService userService) {
        this.userService = userService;
    }

    // Películas pendientes (GET, POST, DELETE)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingMovies(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getPendingMovies(userId));
    }

    @PostMapping("/pending/{movieId}")
    public ResponseEntity<?> addPending(@PathVariable("userId") Long userId,
            @PathVariable("movieId") Long movieId) {
        userService.addPending(userId, movieId);
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
    public ResponseEntity<?> getFavourites(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getFavourites(userId));
    }

    @PostMapping("/favourites/{movieId}")
    public ResponseEntity<?> addFavourite(@PathVariable("userId") Long userId,
            @PathVariable("movieId") Long movieId) {
        userService.addFavourite(userId, movieId);
        return ResponseEntity.ok().body("Película añadida a favoritas correctamente");
    }

    @DeleteMapping("/favourites/{movieId}")
    public ResponseEntity<?> removeFavourite(@PathVariable("userId") Long userId,
            @PathVariable("movieId") Long movieId) {
        userService.removeFavourite(userId, movieId);
        return ResponseEntity.ok().body("Película eliminada de favoritas correctamente");
    }

    // Listas (GET)
    @GetMapping("")
    public ResponseEntity<?> getLists(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getLists(userId));
    }
}
