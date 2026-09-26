package com.vitral.dto;

import java.time.OffsetDateTime;
import com.vitral.enumerations.StatusVerificacaoSebo;

public record AdminSeboResponse(Long id, String nome, String responsavel, boolean contaAtiva,
        StatusVerificacaoSebo status, OffsetDateTime createdAt, OffsetDateTime ultimaAtividade,
        long produtosAtivos, long produtosInativos) {}
