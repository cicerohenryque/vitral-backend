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
        String cep,
        String logradouro,
        String cidade,
        String uf,
        String horarioFuncionamento,
        OffsetDateTime dataCriacao,
        OffsetDateTime ultimaAtividade,
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
                tipoOperacao, null, null, null, null, null, null, null, Boolean.FALSE);
    }

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
            TipoOperacaoSebo tipoOperacao,
            Boolean confirmado) {
        this(id, accountId, nome, email, descricao, telefone, cnpj, statusVerificacao, motivoRejeicao,
                razaoSocialReceita, statusConsultaCnpj, cnpjConsultadoEm, mensagemConsultaCnpj, fotoUrl,
                tipoOperacao, null, null, null, null, null, null, null, confirmado);
    }

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
            TipoOperacaoSebo tipoOperacao,
            String cep,
            String logradouro,
            String cidade,
            String uf,
            String horarioFuncionamento,
            Boolean confirmado) {
        this(id, accountId, nome, email, descricao, telefone, cnpj, statusVerificacao, motivoRejeicao,
                razaoSocialReceita, statusConsultaCnpj, cnpjConsultadoEm, mensagemConsultaCnpj, fotoUrl,
                tipoOperacao, cep, logradouro, cidade, uf, horarioFuncionamento, null, null, confirmado);
    }
}
