package com.vitral.dto;

import jakarta.validation.constraints.Size;

public record RejeitarSugestaoCatalogoRequest(
        @Size(max = 500, message = "O motivo deve ter, no máximo, 500 caracteres.") String motivo) {
}
