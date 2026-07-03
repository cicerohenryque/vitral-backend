package com.vitral.dto;

import jakarta.validation.constraints.Size;

public record SeboRequest(
        @Size(max = 2000) String descricao,
        @Size(max = 20) String telefone,
        @Size(max = 500) String fotoUrl) {
}
