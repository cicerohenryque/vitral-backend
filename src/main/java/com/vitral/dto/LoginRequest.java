package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail ou nome de usuário.") String login,
        @NotBlank(message = "Informe sua senha.") String password) {
}
