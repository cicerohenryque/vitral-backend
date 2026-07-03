package com.vitral.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VendaFisicaRequest(@NotNull @Min(1) Integer quantidade) {
}
