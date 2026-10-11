package com.EnSe.Movienatic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

    public UserNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "No se encontro el usuario con el id "+id);
    }
    public UserNotFoundException(String username) {
        super(HttpStatus.NOT_FOUND, "No se encontro el usuario "+username);
    }
}
