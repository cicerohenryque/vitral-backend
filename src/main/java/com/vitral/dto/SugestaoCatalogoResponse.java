package com.vitral.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.vitral.enumerations.BookGenre;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.StatusSugestaoCatalogo;

public record SugestaoCatalogoResponse(
        Long id,
        Long usuarioId,
        String usuarioNome,
        Long seboId,
        String catalogoNome,
        Long categoriaId,
        String categoriaNome,
        String titulo,
        String autor,
        String descricao,
        Integer ano,
        BigDecimal preco,
        Integer estoque,
        CondicaoProduto condicao,
        BookGenre bookGenre,
        String fotoUrl,
        StatusSugestaoCatalogo status,
        OffsetDateTime enviadaEm,
        OffsetDateTime decididaEm,
        String motivoRejeicao,
        Long produtoCriadoId) {
}
