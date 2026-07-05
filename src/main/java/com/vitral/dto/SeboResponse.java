package com.vitral.dto;

import com.vitral.enumerations.StatusVerificacaoSebo;

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
        String fotoUrl) {
}
