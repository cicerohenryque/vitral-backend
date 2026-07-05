package com.vitral.dto;

import java.time.OffsetDateTime;
import com.vitral.enumerations.StatusDocumentoSebo;
import com.vitral.enumerations.TipoDocumentoSebo;

public record DocumentoVerificacaoSeboResponse(Long id, TipoDocumentoSebo tipoDocumento,
        String arquivoUrl, StatusDocumentoSebo status, OffsetDateTime enviadoEm,
        OffsetDateTime analisadoEm, Long analisadoPorId, String motivoRejeicao) {
}
