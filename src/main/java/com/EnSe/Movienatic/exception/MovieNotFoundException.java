package com.EnSe.Movienatic.exception;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super("No se encontro la película con el id " + id);
    }

    public MovieNotFoundException(String title) {
        super("No se encontro la película " + title);
    }
}