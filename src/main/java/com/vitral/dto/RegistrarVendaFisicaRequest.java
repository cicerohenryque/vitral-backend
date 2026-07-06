package com.vitral.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarVendaFisicaRequest(
        @NotNull Long produtoId,
        @NotNull @Min(1) Integer quantidade,
        @NotNull @DecimalMin("0.00") BigDecimal valorUnitario,
        @Size(max = 500) String observacao) {
}
