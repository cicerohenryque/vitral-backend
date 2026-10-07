package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminProdutoResponse(Long id, String titulo, BigDecimal preco, Integer estoque, Long seboId,
        String seboNome, boolean ativo, OffsetDateTime createdAt) {}
