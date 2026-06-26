package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.CondicaoProduto;

public record ProdutoResponse(
        Long id,
        Long seboId,
        Long categoriaId,
        String categoriaNome,
        String titulo,
        String autor,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        CondicaoProduto condicao,
        String fotoUrl,
        Boolean ativo) {
}
