CREATE TABLE mensagem (
    id               BIGSERIAL PRIMARY KEY,
    remetente_id     BIGINT NOT NULL REFERENCES account(id),
    destinatario_id  BIGINT NOT NULL REFERENCES account(id),
    conteudo         TEXT NOT NULL,
    lida             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
