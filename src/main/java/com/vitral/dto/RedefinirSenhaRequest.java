package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "Informe o token de recuperação.") String token,
        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.") String password) {
}
