package com.EnSe.Movienatic.controller;

import com.EnSe.Movienatic.dto.LoginDto;
import com.EnSe.Movienatic.dto.RegisterDto;
import com.EnSe.Movienatic.service.AuthService;

import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @Valid activa la validación del DTO (@NotBlank/@Email): si falla, la
    // excepción la recoge GlobalExceptionHandler -> 400.
    // Los errores de negocio (duplicado, credenciales mal) también los resuelve
    // el handler, así que aquí no hay try/catch
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDto registerDto) {
        authService.registrarUsuario(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        authService.login(loginDto);
        return ResponseEntity.ok(Map.of("message", "Login correcto"));
    }
}
