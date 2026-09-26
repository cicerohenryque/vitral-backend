package com.vitral.dto;

import jakarta.validation.constraints.NotNull;

public record AdminStatusRequest(@NotNull(message = "Informe o status desejado.") Boolean ativo) {
}
