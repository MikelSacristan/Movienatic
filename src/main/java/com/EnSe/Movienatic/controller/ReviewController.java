package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.ReviewRequest;
import com.EnSe.Movienatic.dto.UpdateReviewRequest;
import com.EnSe.Movienatic.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/reviews")
public class ReviewController {

    private final UserService userService;

    public ReviewController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    public ResponseEntity<?> addReview(@PathVariable("userId") Long userId,
            @RequestBody ReviewRequest request) {
        userService.addReview(userId, request.getMovieId(), request.getContent(), request.getRating());
        return ResponseEntity.ok().body("Reseña añadida correctamente");
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(@PathVariable("userId") Long userId,
            @PathVariable("reviewId") Long reviewId, @RequestBody UpdateReviewRequest request) {
        userService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok().body("Reseña actualizada correctamente");
    }

    @GetMapping("")
    public ResponseEntity<?> getReviews(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getReviews(userId));
    }
}
