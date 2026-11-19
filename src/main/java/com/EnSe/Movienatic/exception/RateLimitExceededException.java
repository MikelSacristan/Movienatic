package com.EnSe.Movienatic.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String mensaje, Long segundosEspera) {
        super(mensaje + "Debe esperar" + segundosEspera + "para acceder a este endpoint de nuevo");
    }
}
