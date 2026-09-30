package com.EnSe.Movienatic.exception;
public class DuplicatedUserException extends Exception{
    private final String username;
    private final String email;

    public DuplicatedUserException(String username, String email) {
        this.username = username;
        this.email=email;
    }

    public String getUsername() {
        return username;
    }
    public String getEmail(){
        return email;
    }
}
