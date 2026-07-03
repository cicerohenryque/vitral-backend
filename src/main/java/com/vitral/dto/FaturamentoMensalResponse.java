package com.vitral.dto;

import java.math.BigDecimal;

public record FaturamentoMensalResponse(int ano, int mes, BigDecimal total) {
}
