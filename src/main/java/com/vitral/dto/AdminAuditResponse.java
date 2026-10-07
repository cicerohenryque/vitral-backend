package com.vitral.dto;

import java.time.OffsetDateTime;

public record AdminAuditResponse(Long id, Long adminId, String adminNome, String adminEmail, String acao,
        String entidade, Long entidadeId, String descricao, OffsetDateTime createdAt) {}
