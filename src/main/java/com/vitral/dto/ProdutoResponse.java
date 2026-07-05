package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.CondicaoProduto;

public record ProdutoResponse(
        Long id,
        Long seboId,
        Long categoriaId,
        String categoriaNome,
        Long catalogoLivroId,
        String isbn,
        String titulo,
        String autor,
        String descricao,
        BigDecimal preco,
        BigDecimal precoPromocional,
        Integer estoque,
        CondicaoProduto condicao,
        String fotoUrl,
        Boolean ativo) {
}
