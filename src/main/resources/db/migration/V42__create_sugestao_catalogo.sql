CREATE TABLE sugestao_catalogo (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id         BIGINT NOT NULL REFERENCES account(id) ON DELETE CASCADE,
    sebo_id            BIGINT NOT NULL REFERENCES sebo(id) ON DELETE CASCADE,
    categoria_id       BIGINT NOT NULL REFERENCES categoria(id),
    titulo             VARCHAR(255) NOT NULL,
    autor              VARCHAR(255),
    descricao          VARCHAR(2000),
    ano                INTEGER,
    preco              NUMERIC(12,2) NOT NULL,
    estoque            INTEGER NOT NULL DEFAULT 1,
    condicao           VARCHAR(20) NOT NULL,
    book_genre         VARCHAR(20),
    foto_url           VARCHAR(500),
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    decidido_por_id    BIGINT REFERENCES account(id) ON DELETE SET NULL,
    decidido_em        TIMESTAMPTZ,
    motivo_rejeicao    VARCHAR(500),
    produto_criado_id  BIGINT REFERENCES produto(id) ON DELETE SET NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_sugestao_catalogo_status CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
    CONSTRAINT ck_sugestao_catalogo_estoque CHECK (estoque >= 0),
    CONSTRAINT ck_sugestao_catalogo_preco CHECK (preco >= 0)
);

CREATE INDEX ix_sugestao_catalogo_usuario_data
    ON sugestao_catalogo (usuario_id, created_at DESC);

CREATE INDEX ix_sugestao_catalogo_sebo_status_data
    ON sugestao_catalogo (sebo_id, status, created_at DESC);
