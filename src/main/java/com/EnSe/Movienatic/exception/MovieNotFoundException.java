package com.EnSe.Movienatic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MovieNotFoundException extends ResponseStatusException {

    public MovieNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "No se encontro la película con el id "+id);
    }
}