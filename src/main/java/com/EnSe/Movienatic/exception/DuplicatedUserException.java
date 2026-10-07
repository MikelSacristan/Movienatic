package com.EnSe.Movienatic.exception;

// RuntimeException: no obliga a declarar "throws" en los controllers.
// El mensaje se hereda con super() para que e.getMessage() funcione
public class DuplicatedUserException extends RuntimeException {

    private final String username;
    private final String email;

    public DuplicatedUserException(String username, String email) {
        super("El usuario '" + username + "' o el email '" + email + "' ya están registrados");
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
