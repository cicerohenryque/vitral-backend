package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.BookGenre;
import com.vitral.enumerations.CondicaoProduto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AprovarSugestaoCatalogoRequest(
        @NotBlank(message = "Informe o título do produto.") @Size(max = 255) String titulo,
        @NotNull(message = "Selecione a categoria do produto.") Long categoriaId,
        BookGenre bookGenre,
        @Size(max = 255) String autor,
        @Size(max = 2000) String descricao,
        @Min(0) @Max(2200) Integer ano,
        @NotNull(message = "Confirme o preço do produto.") @DecimalMin("0.0")
        @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @NotNull(message = "Confirme a quantidade disponível.") @Min(0) Integer estoque,
        @NotNull(message = "Confirme a condição do produto.") CondicaoProduto condicao,
        @Size(max = 500) String fotoUrl) {
}
