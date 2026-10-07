package com.EnSe.Movienatic.exception;

public class UserNotFoundException extends RuntimeException {

    private final String username;

    public UserNotFoundException(String username) {
        super("No existe el usuario: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
