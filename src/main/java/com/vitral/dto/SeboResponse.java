package com.vitral.dto;

import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.enumerations.StatusConsultaCnpj;
import com.vitral.enumerations.TipoOperacaoSebo;
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
        String fotoUrl,
        TipoOperacaoSebo tipoOperacao,
        Boolean confirmado) {

    public SeboResponse(
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
            String fotoUrl,
            TipoOperacaoSebo tipoOperacao) {
        this(id, accountId, nome, email, descricao, telefone, cnpj, statusVerificacao, motivoRejeicao,
                razaoSocialReceita, statusConsultaCnpj, cnpjConsultadoEm, mensagemConsultaCnpj, fotoUrl,
                tipoOperacao, Boolean.FALSE);
    }
}
