package com.EnSe.Movienatic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MovieNotFoundException extends ResponseStatusException {

    public MovieNotFoundException(HttpStatus status, String message) {
        super(status, message);
    }
}