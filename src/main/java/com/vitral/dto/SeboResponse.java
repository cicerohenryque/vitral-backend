package com.vitral.dto;

import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.enumerations.StatusConsultaCnpj;
import java.time.OffsetDateTime;

public record SeboResponse(
        Long id,
        Long accountId,
        String nome,
        String email,
        String descricao,
        String telefone,
        String cnpj,
        StatusVerificacaoSebo statusVerificacao,
        String motivoRejeicao,
        String razaoSocialReceita,
        StatusConsultaCnpj statusConsultaCnpj,
        OffsetDateTime cnpjConsultadoEm,
        String mensagemConsultaCnpj,
        String fotoUrl) {
}
