package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.vitral.enumerations.StatusPedido;

public record PedidoResponse(
        Long id,
        Long accountId,
        Long seboId,
        StatusPedido status,
        BigDecimal total,
        OffsetDateTime createdAt,
        List<PedidoItemResponse> itens) {
}
