package com.vitral.dto;

import com.vitral.enumerations.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Informe seu nome.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.") String name,
        @NotBlank @Pattern(
                regexp = "^[a-z0-9._-]{3,30}$",
                message = "O nome de usuário deve ter de 3 a 30 caracteres e usar apenas letras minúsculas, números, ponto, hífen ou underline, sem espaços.")
        String username,
        @NotBlank(message = "Informe seu e-mail.") @Email(message = "Informe um e-mail válido.") String email,
        @NotBlank(message = "Informe uma senha.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.") String password,
        @NotNull(message = "Selecione o tipo da conta.") AccountType type) {
}
