package com.vitral.dto;

import com.vitral.enumerations.AccountType;

public record AccountResponse(
        Long id,
        String name,
        String email,
        AccountType type,
        String fotoUrl) {
}
