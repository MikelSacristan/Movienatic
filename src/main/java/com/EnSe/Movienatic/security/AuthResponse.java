package com.EnSe.Movienatic.security;

public record AuthResponse(String accessToken, String refreshToken, String type, String email, String username) {
    public AuthResponse(String accessToken, String refreshToken, String email, String username) {
        this(accessToken, refreshToken, "Bearer", email, username);
    }
}