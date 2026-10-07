package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MensagemRequest(
        @NotNull(message = "Informe o destinatário da mensagem.") Long destinatarioId,
        @NotBlank(message = "Escreva uma mensagem antes de enviar.")
        @Size(max = 2000, message = "A mensagem deve ter, no máximo, 2.000 caracteres.") String conteudo) {
}
