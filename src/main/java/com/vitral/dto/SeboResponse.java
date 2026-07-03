package com.vitral.dto;

public record SeboResponse(
        Long id,
        Long accountId,
        String nome,
        String email,
        String descricao,
        String telefone,
        String fotoUrl) {
}
