package com.EnSe.Movienatic.exception;

public class DuplicatedMovieException extends RuntimeException {

    private final String title;

    public DuplicatedMovieException(String title) {
        super("La película '" + title + "' ya existe en la base de datos");
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
