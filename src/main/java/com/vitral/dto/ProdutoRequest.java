package com.vitral.dto;

import java.math.BigDecimal;

import com.vitral.enumerations.CondicaoProduto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequest(
        @Size(max = 20) String isbn,
        @NotBlank @Size(max = 255) String titulo,
        Long categoriaId,
        @Size(max = 255) String autor,
        @Size(max = 2000) String descricao,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal preco,
        @Min(0) Integer estoque,
        @NotNull CondicaoProduto condicao,
        @Size(max = 500) String fotoUrl) {
}
