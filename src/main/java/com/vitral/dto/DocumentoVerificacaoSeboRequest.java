package com.vitral.dto;

import com.vitral.enumerations.TipoDocumentoSebo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoVerificacaoSeboRequest(
        @NotNull(message = "Informe o tipo do documento.") TipoDocumentoSebo tipoDocumento,
        @NotBlank(message = "Envie um documento antes de continuar.")
        @Size(max = 500, message = "A URL do documento deve ter, no máximo, 500 caracteres.") String arquivoUrl) {
}
