package com.EnSe.Movienatic.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EnSe.Movienatic.dto.MovieDto;
import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.model.User;
import com.EnSe.Movienatic.service.UserService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Peliculas pendientes (GET, POST, DELETE)
    @GetMapping("/{userId}/pending-movies")
    public ResponseEntity<?> getPendingMovies(@PathVariable Long userId) {
        List<MovieDto> pendingMovies = userService.getPendingMovies(userId);
        return ResponseEntity.ok(pendingMovies);
    }

    @PostMapping("/{userId}/pending-movies/{movieId}")
    public ResponseEntity<?> addPending(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.addPending(userId, movieId);

        return ResponseEntity.ok().body("Película añadida a pendientes correctamente");
    }

    @DeleteMapping("/{userId}/pending-movies/{movieId}")
    public ResponseEntity<?> removePending(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.removePending(userId, movieId);

        return ResponseEntity.ok().body("Película eliminada de pendientes correctamente");
    }

    // Favoritas (GET, POST, DELETE)
    @GetMapping("/{userId}/favourites")
    public ResponseEntity<?> getFavourites(@PathVariable Long userId) {
        List<MovieDto> favourites = userService.getFavourites(userId);
        return ResponseEntity.ok(favourites);
    }

    @PostMapping("/{userId}/favourites/{movieId}")
    public ResponseEntity<?> addFavourite(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.addFavourite(userId, movieId);

        return ResponseEntity.ok().body("Película añadida a favoritas correctamente");
    }

    @DeleteMapping("/{userId}/favourites/{movieId}")
    public ResponseEntity<?> removeFavourite(@PathVariable Long userId, @PathVariable Long movieId) {
        userService.removeFavourite(userId, movieId);

        return ResponseEntity.ok().body("Película eliminada de favoritas correctamente");
    }

    // Siguiendo (GET, POST, DELETE)
    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId) {
        List<UserDto> following = userService.getFollowing(userId);
        return ResponseEntity.ok(following);
    }

    @PostMapping("/{userId}/following/{userIdToFollow}")
    public ResponseEntity<?> follow(@PathVariable Long userId, @PathVariable Long userIdToFollow) {
        userService.followUser(userId, userIdToFollow);

        return ResponseEntity.ok().body("Usuario seguido correctamente");
    }

    @DeleteMapping("/{userId}/following/{userIdToUnfollow}")
    public ResponseEntity<?> unfollow(@PathVariable Long userId, @PathVariable Long userIdToUnfollow) {
        userService.unfollowUser(userId, userIdToUnfollow);

        return ResponseEntity.ok().body("Usuario dejado de seguir correctamente");
    }

    // Solo consulta (GET)

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId) {
        List<UserDto> followers = userService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{userId}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long userId) {
        List<?> reviews = userService.getReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{userId}/lists")
    public ResponseEntity<?> getLists(@PathVariable Long userId) {
        List<?> lists = userService.getLists(userId);
        return ResponseEntity.ok(lists);
    }
}
