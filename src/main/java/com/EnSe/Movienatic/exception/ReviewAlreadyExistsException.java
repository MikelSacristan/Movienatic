package com.EnSe.Movienatic.exception;

public class ReviewAlreadyExistsException extends RuntimeException {

    public ReviewAlreadyExistsException(Long userId, Long movieId) {
        super("El usuario con id " + userId + " ya ha escrito una reseña para la película con id " + movieId);
    }

}
