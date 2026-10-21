package com.EnSe.Movienatic.exception;

public class ReviewNotOwnedException extends RuntimeException {

    public ReviewNotOwnedException(Long userId, Long reviewId) {
        super("La reseña con id " + reviewId + " no pertenece al usuario con id " + userId);
    }

}
