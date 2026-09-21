package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfertaRequest(
        @NotNull(message = "Selecione o produto da oferta.") Long produtoId,
        @NotNull(message = "Informe o preço promocional.")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preço promocional deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2, message = "Informe um preço promocional válido com até duas casas decimais.") BigDecimal precoPromocional,
        @Size(max = 500) String descricao,
        OffsetDateTime inicioEm,
        OffsetDateTime fimEm,
        Boolean ativa) {
}
