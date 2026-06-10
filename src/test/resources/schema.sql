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
