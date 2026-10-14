package com.EnSe.Movienatic.exception;

public class FavoriteMovieNotFoundException extends RuntimeException {

    public FavoriteMovieNotFoundException(Long userId, Long movieId) {
        super("El usuario con id " + userId.toString() + " no tiene la película favorita con id "
                + movieId.toString());
    }
}