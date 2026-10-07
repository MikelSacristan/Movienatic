package com.EnSe.Movienatic.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Un único punto donde todas las excepciones se convierten en respuesta JSON.
// Así los controllers no necesitan try/catch y todos los errores salen con la
// misma forma: { "status": ..., "error": ..., "message": ... }
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409: registro de un usuario que ya existe
    @ExceptionHandler(DuplicatedUserException.class)
    public ResponseEntity<Map<String, Object>> usuarioDuplicado(DuplicatedUserException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    // 409: importación de una película que ya está en la BD
    @ExceptionHandler(DuplicatedMovieException.class)
    public ResponseEntity<Map<String, Object>> peliculaDuplicada(DuplicatedMovieException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    // 404: recurso que no existe
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> usuarioNoEncontrado(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 401: login con credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> credencialesIncorrectas(BadCredentialsException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // 400: fallos de @Valid (@NotBlank, @Email...) en el body de la petición.
    // Spring lanza esta excepción automáticamente; aquí la traducimos a un
    // mensaje legible campo por campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, mensaje);
    }

    // Forma común de respuesta de error
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
