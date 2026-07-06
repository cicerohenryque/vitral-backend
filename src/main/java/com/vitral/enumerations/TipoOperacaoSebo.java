package com.vitral.enumerations;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoOperacaoSebo {
    VIRTUAL,
    FISICO,
    HIBRIDO;

    @JsonCreator
    public static TipoOperacaoSebo from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TipoOperacaoSebo.valueOf(value.trim().toUpperCase());
    }
}
