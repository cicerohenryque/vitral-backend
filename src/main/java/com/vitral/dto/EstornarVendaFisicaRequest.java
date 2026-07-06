package com.vitral.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstornarVendaFisicaRequest(
        @NotBlank @Size(max = 500) String motivo) {
}
