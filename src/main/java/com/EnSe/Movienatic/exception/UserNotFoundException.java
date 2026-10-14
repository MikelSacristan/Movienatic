package com.EnSe.Movienatic.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("No se encontro el usuario con el id " + id);
    }

    public UserNotFoundException(String username) {
        super("No se encontro el usuario " + username);
    }
}
