package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.UserDto;
import com.EnSe.Movienatic.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    // Tamaño de página por defecto: 4 columnas x 5 filas = 20 películas
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Paginación offset: ?page=0&size=20 (permite saltar a cualquier página)
    @GetMapping("")
    public Page<UserDto> getUsers(@PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    // Siguiendo (GET, POST, DELETE)
    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getFollowing(userId));
    }

    @PostMapping("/{userId}/following/{userIdToFollow}")
    public ResponseEntity<?> follow(@PathVariable("userId") Long userId,
            @PathVariable("userIdToFollow") Long userIdToFollow) {
        userService.followUser(userId, userIdToFollow);
        return ResponseEntity.ok().body("Usuario seguido correctamente");
    }

    @DeleteMapping("/{userId}/following/{userIdToUnfollow}")
    public ResponseEntity<?> unfollow(@PathVariable("userId") Long userId,
            @PathVariable("userIdToUnfollow") Long userIdToUnfollow) {
        userService.unfollowUser(userId, userIdToUnfollow);
        return ResponseEntity.ok().body("Usuario dejado de seguir correctamente");
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }
}
