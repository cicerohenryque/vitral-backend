package com.vitral.dto;

import java.math.BigDecimal;

public record FaturamentoMensalResponse(
        int ano,
        int mes,
        BigDecimal vendasOnline,
        BigDecimal vendasFisicas,
        BigDecimal estornos,
        BigDecimal reembolsos,
        BigDecimal estornosReembolsos,
        BigDecimal total,
        BigDecimal totalLiquido) {
}
