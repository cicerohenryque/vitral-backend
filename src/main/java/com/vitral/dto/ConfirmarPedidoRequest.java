package com.vitral.dto;

import com.vitral.enumerations.FormaPagamento;

import jakarta.validation.constraints.NotNull;

public record ConfirmarPedidoRequest(
        @NotNull(message = "Selecione a forma de pagamento.") FormaPagamento formaPagamento,
        String numeroCartao) {
}
