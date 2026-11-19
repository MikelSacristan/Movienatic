package com.EnSe.Movienatic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO con validaciones para el login de un usuario
public record LoginDto(@NotBlank @Email String email,
        @NotBlank String password) {

}
