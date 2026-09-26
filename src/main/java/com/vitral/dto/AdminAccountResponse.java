package com.vitral.dto;

import java.time.OffsetDateTime;

import com.vitral.enumerations.AccountType;

public record AdminAccountResponse(Long id, String name, String email, AccountType type,
        boolean ativo, OffsetDateTime createdAt) {
}
