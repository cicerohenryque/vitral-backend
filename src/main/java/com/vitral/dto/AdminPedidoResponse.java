package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.vitral.enumerations.StatusPagamento;
import com.vitral.enumerations.StatusPedido;

public record AdminPedidoResponse(Long id, Long accountId, String usuarioNome, Long seboId, String seboNome,
        StatusPedido status, StatusPagamento statusPagamento, BigDecimal total, OffsetDateTime createdAt,
        OffsetDateTime updatedAt, OffsetDateTime confirmadoEm, OffsetDateTime pagoEm,
        OffsetDateTime canceladoEm, OffsetDateTime reembolsadoEm, java.util.List<Item> itens) {
    public record Item(Long produtoId, String titulo, BigDecimal precoUnitario, Integer quantidade) {}
}
