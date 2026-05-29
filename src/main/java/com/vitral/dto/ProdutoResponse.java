package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.CondicaoProduto;

public record ProdutoResponse(
        Long id,
        Long seboId,
        String titulo,
        String autor,
        String descricao,
        BigDecimal preco,
        CondicaoProduto condicao,
        String fotoUrl,
        Boolean ativo) {
}
