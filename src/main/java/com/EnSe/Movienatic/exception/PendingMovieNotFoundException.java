package com.EnSe.Movienatic.exception;

public class PendingMovieNotFoundException extends RuntimeException {

    public PendingMovieNotFoundException(Long userId, Long movieId) {
        super("El usuario con id " + userId.toString() + " no tiene la película pendiente con id "
                + movieId.toString());
    }
}