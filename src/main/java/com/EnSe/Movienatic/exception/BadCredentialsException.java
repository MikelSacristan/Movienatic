package com.EnSe.Movienatic.exception;

// Un único mensaje para "no existe" y "password mal": así un atacante no puede
// averiguar qué emails existen en la BD (enumeración de usuarios)
public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}
