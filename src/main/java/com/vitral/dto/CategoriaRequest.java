package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "Informe o nome da categoria.")
        @Size(max = 120, message = "O nome da categoria deve ter, no máximo, 120 caracteres.") String nome,
        @Size(max = 500, message = "A descrição deve ter, no máximo, 500 caracteres.") String descricao) {
}
