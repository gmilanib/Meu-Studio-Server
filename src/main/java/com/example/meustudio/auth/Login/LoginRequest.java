package com.example.meustudio.auth.Login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
                @NotBlank(message = "Login em branco") String username,
                @NotBlank(message = "Senha em branco") String password) {
}
