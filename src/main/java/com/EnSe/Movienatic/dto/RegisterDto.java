package com.EnSe.Movienatic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// DTO con validaciones para la creación de un usuario
public record RegisterDto(@NotBlank String username, @NotBlank @Email String email,
        @NotBlank String password) {

}
