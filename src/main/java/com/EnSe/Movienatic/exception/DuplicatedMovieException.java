package com.EnSe.Movienatic.exception;
public class DuplicatedMovieException extends Exception{
    private final String title;

    public DuplicatedMovieException(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
