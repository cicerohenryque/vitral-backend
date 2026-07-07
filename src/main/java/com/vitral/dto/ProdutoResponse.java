package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
        Boolean ativo,
        OffsetDateTime dataPublicacao) {

    public ProdutoResponse(
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
        this(id, seboId, categoriaId, categoriaNome, catalogoLivroId, isbn, titulo, autor, descricao, preco,
                precoPromocional, estoque, condicao, fotoUrl, ativo, null);
    }
}
