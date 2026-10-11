package com.EnSe.Movienatic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FavoriteMovieNotFoundException extends ResponseStatusException {

    public FavoriteMovieNotFoundException(Long userId, Long movieId) {
        super(HttpStatus.NOT_FOUND, "El usuario con id "+userId.toString()+" no tiene la película favorita con id "+movieId.toString());
    }
}