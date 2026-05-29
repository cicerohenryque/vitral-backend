package com.vitral.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SeboRequest(
        @Size(max = 2000) String descricao,
        @Size(max = 20) String telefone,
        @Size(max = 9) String cep,
        @Size(max = 255) String logradouro,
        @Size(max = 120) String cidade,
        @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve conter 2 letras maiusculas") String uf,
        @Size(max = 255) String horarioFuncionamento,
        @Size(max = 500) String fotoUrl) {
}
