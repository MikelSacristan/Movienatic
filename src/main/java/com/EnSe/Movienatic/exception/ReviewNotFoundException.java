package com.EnSe.Movienatic.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String reviewId) {
        super(reviewId);
    }

}
