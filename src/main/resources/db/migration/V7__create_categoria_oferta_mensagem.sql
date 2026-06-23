CREATE TABLE categoria (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL,
    descricao   VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_categoria_nome UNIQUE (nome),
    CONSTRAINT uk_categoria_slug UNIQUE (slug)
);

ALTER TABLE produto
    ADD COLUMN categoria_id BIGINT;

ALTER TABLE produto
    ADD CONSTRAINT fk_produto_categoria
    FOREIGN KEY (categoria_id) REFERENCES categoria (id);

CREATE INDEX ix_produto_categoria ON produto (categoria_id);

CREATE TABLE oferta (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id         BIGINT         NOT NULL,
    preco_promocional  NUMERIC(12, 2) NOT NULL,
    descricao          VARCHAR(500),
    inicio_em          TIMESTAMPTZ,
    fim_em             TIMESTAMPTZ,
    ativa              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ    NOT NULL,
    updated_at         TIMESTAMPTZ    NOT NULL,
    CONSTRAINT fk_oferta_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT ck_oferta_preco CHECK (preco_promocional > 0),
    CONSTRAINT ck_oferta_periodo CHECK (fim_em IS NULL OR inicio_em IS NULL OR fim_em >= inicio_em)
);

CREATE INDEX ix_oferta_produto ON oferta (produto_id);
CREATE INDEX ix_oferta_ativa_periodo ON oferta (ativa, inicio_em, fim_em);

CREATE TABLE mensagem (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    remetente_id    BIGINT        NOT NULL,
    destinatario_id BIGINT        NOT NULL,
    conteudo        VARCHAR(2000) NOT NULL,
    lida            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (remetente_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_destinatario FOREIGN KEY (destinatario_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT ck_mensagem_destinatario CHECK (remetente_id <> destinatario_id)
);

CREATE INDEX ix_mensagem_remetente ON mensagem (remetente_id);
CREATE INDEX ix_mensagem_destinatario ON mensagem (destinatario_id);
CREATE INDEX ix_mensagem_conversa ON mensagem (remetente_id, destinatario_id, created_at);
