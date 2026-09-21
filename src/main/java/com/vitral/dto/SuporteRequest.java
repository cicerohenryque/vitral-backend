package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuporteRequest(
        @NotBlank(message = "Informe o assunto da solicitação.")
        @Size(max = 255, message = "O assunto deve ter, no máximo, 255 caracteres.") String assunto,
        @NotBlank(message = "Descreva como podemos ajudar.")
        @Size(max = 2000, message = "A mensagem deve ter, no máximo, 2.000 caracteres.") String mensagem) {
}
