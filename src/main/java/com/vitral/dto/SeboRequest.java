package com.vitral.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.vitral.enumerations.TipoOperacaoSebo;

public record SeboRequest(
        @Size(max = 2000) String descricao,
        @Size(max = 20) String telefone,
        @NotBlank @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter 14 digitos") String cnpj,
        @Size(max = 500) String fotoUrl,
        TipoOperacaoSebo tipoOperacao) {
}
