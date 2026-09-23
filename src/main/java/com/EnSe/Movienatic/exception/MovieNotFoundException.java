package com.EnSe.Movienatic.exception;
public class MovieNotFoundException extends Exception{
    private final String title;

    public MovieNotFoundException(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
