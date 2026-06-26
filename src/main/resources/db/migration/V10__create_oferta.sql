CREATE TABLE oferta (
    id                  BIGSERIAL PRIMARY KEY,
    produto_id          BIGINT NOT NULL REFERENCES produto(id),
    preco_original      NUMERIC(12, 2) NOT NULL,
    preco_promocional   NUMERIC(12, 2) NOT NULL,
    descricao           VARCHAR(500),
    inicio_em           TIMESTAMP,
    fim_em              TIMESTAMP,
    ativa               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);
