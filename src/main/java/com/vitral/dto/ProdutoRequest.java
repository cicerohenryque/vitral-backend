package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.BookGenre;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @NotBlank(message = "Informe o título do produto.")
        @Size(max = 255, message = "O título deve ter, no máximo, 255 caracteres.") String titulo,
        @NotNull(message = "Selecione uma categoria para o produto.") Long categoriaId,
        BookGenre bookGenre,
        @Size(max = 255) String autor,
        @Size(max = 2000) String descricao,
        @Min(0) @Max(2200) Integer ano,
        @NotNull(message = "Informe o preço do produto.")
        @DecimalMin(value = "0.0", inclusive = true, message = "O preço não pode ser negativo.")
        @Digits(integer = 10, fraction = 2, message = "Informe um preço válido com até duas casas decimais.") BigDecimal preco,
        @Min(value = 0, message = "O estoque não pode ser negativo.") Integer estoque,
        @NotNull(message = "Selecione a condição do produto.") CondicaoProduto condicao,
        @Size(max = 500) String fotoUrl,
        Boolean classico,
        Boolean lancamento) {

    public ProdutoRequest(String titulo, Long categoriaId, String autor, String descricao,
            BigDecimal preco, Integer estoque, CondicaoProduto condicao, String fotoUrl) {
        this(titulo, categoriaId, null, autor, descricao, null, preco, estoque, condicao, fotoUrl, false, false);
    }

    public ProdutoRequest(String titulo, Long categoriaId, BookGenre bookGenre, String autor,
            String descricao, BigDecimal preco, Integer estoque, CondicaoProduto condicao, String fotoUrl) {
        this(titulo, categoriaId, bookGenre, autor, descricao, null, preco, estoque, condicao, fotoUrl, false, false);
    }

    public ProdutoRequest(String titulo, Long categoriaId, BookGenre bookGenre, String autor,
            String descricao, BigDecimal preco, Integer estoque, CondicaoProduto condicao, String fotoUrl,
            Boolean classico, Boolean lancamento) {
        this(titulo, categoriaId, bookGenre, autor, descricao, null, preco, estoque, condicao, fotoUrl, classico,
                lancamento);
    }
}
