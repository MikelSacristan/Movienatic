package com.EnSe.Movienatic.exception;
public class DuplicatedUserException extends Exception{
    private final String username;

    public DuplicatedUserException(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
