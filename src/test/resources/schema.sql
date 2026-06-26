DROP TABLE IF EXISTS pedido_item;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS cesta_item;
DROP TABLE IF EXISTS favoritos;
DROP TABLE IF EXISTS token_verificacao_email;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS sebo;
DROP TABLE IF EXISTS account;

CREATE TABLE account (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    type             VARCHAR(20)  NOT NULL,
    email_verificado BOOLEAN      NOT NULL DEFAULT FALSE,
    foto_url         VARCHAR(500),
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT uk_account_email UNIQUE (email),
    CONSTRAINT ck_account_type CHECK (type IN ('SEBO', 'USUARIO'))
);

CREATE TABLE sebo (
    id                     BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id             BIGINT       NOT NULL,
    descricao              VARCHAR(2000),
    telefone               VARCHAR(20),
    cep                    VARCHAR(9),
    logradouro             VARCHAR(255),
    cidade                 VARCHAR(120),
    uf                     VARCHAR(2),
    horario_funcionamento  VARCHAR(255),
    foto_url               VARCHAR(500),
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT uk_sebo_account UNIQUE (account_id),
    CONSTRAINT fk_sebo_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);

CREATE TABLE produto (
    id          BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sebo_id     BIGINT         NOT NULL,
    titulo      VARCHAR(255)   NOT NULL,
    autor       VARCHAR(255),
    descricao   VARCHAR(2000),
    preco       NUMERIC(12, 2) NOT NULL,
    condicao    VARCHAR(20)    NOT NULL,
    foto_url    VARCHAR(500),
    ativo       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,
    CONSTRAINT fk_produto_sebo FOREIGN KEY (sebo_id) REFERENCES sebo (id) ON DELETE CASCADE,
    CONSTRAINT ck_produto_condicao CHECK (condicao IN ('NOVO', 'USADO', 'SEMINOVO')),
    CONSTRAINT ck_produto_preco CHECK (preco >= 0)
);

CREATE TABLE token_verificacao_email (
    id         BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT      NOT NULL,
    token      VARCHAR(36) NOT NULL,
    expira_em  TIMESTAMP   NOT NULL,
    usado      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL,
    CONSTRAINT uk_token_verificacao UNIQUE (token),
    CONSTRAINT fk_token_verificacao_account FOREIGN KEY (account_id)
        REFERENCES account (id) ON DELETE CASCADE
);

CREATE TABLE favoritos (
    id         BIGINT    NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT    NOT NULL,
    produto_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_favorito UNIQUE (account_id, produto_id),
    CONSTRAINT fk_favorito_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE
);

CREATE TABLE cesta_item (
    id         BIGINT    NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT    NOT NULL,
    produto_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_cesta_item UNIQUE (account_id, produto_id),
    CONSTRAINT fk_cesta_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_cesta_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE
);

CREATE TABLE pedido (
    id         BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT         NOT NULL,
    sebo_id    BIGINT         NOT NULL,
    status     VARCHAR(30)    NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
    total      NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    CONSTRAINT fk_pedido_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_pedido_sebo    FOREIGN KEY (sebo_id)    REFERENCES sebo (id),
    CONSTRAINT ck_pedido_total   CHECK (total >= 0)
);

CREATE TABLE pedido_item (
    id               BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id        BIGINT         NOT NULL,
    produto_id       BIGINT         NOT NULL,
    titulo_snapshot  VARCHAR(255)   NOT NULL,
    preco_snapshot   NUMERIC(12, 2) NOT NULL,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    CONSTRAINT fk_pedido_item_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_produto FOREIGN KEY (produto_id) REFERENCES produto (id)
);
