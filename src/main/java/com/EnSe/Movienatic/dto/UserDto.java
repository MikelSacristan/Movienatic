package com.EnSe.Movienatic.dto;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @JsonView(PublicView.class) @NotBlank String username,
        @JsonView(PrivateView.class) @NotBlank @Email String email,
        @JsonView(PrivateView.class) @NotBlank String password) {

    public interface PublicView {
    }

    public interface DetailedView extends PublicView {
    }

    public interface PrivateView {
    }
}