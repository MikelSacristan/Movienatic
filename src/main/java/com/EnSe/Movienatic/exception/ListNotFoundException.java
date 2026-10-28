package com.EnSe.Movienatic.exception;

public class ListNotFoundException extends RuntimeException{

    public ListNotFoundException(Long id) {
        super("No se encontro la lista con el id " + id);
    }
    
}
