DROP TABLE IF EXISTS pedido_item;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS movimentacao_estoque;
DROP TABLE IF EXISTS auditoria_verificacao_sebo;
DROP TABLE IF EXISTS documento_verificacao_sebo;
DROP TABLE IF EXISTS mensagem;
DROP TABLE IF EXISTS cesta_item;
DROP TABLE IF EXISTS favoritos;
DROP TABLE IF EXISTS oferta;
DROP TABLE IF EXISTS token_verificacao_email;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS catalogo_livro;
DROP TABLE IF EXISTS categoria;
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
    auth_version     INTEGER      NOT NULL DEFAULT 0,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    CONSTRAINT uk_account_email UNIQUE (email),
    CONSTRAINT ck_account_type CHECK (type IN ('SEBO', 'USUARIO', 'ADMIN'))
);

CREATE TABLE sebo (
    id                     BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id             BIGINT       NOT NULL,
    descricao              VARCHAR(2000),
    telefone               VARCHAR(20),
    cnpj                   VARCHAR(14),
    status_verificacao     VARCHAR(20) NOT NULL DEFAULT 'VERIFICADO',
    verificado_em          TIMESTAMP,
    motivo_rejeicao        VARCHAR(500),
    foto_url               VARCHAR(500),
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT uk_sebo_account UNIQUE (account_id),
    CONSTRAINT fk_sebo_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);

CREATE TABLE documento_verificacao_sebo (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sebo_id BIGINT NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    arquivo_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    enviado_em TIMESTAMP NOT NULL,
    analisado_em TIMESTAMP,
    analisado_por BIGINT,
    motivo_rejeicao VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_documento_sebo FOREIGN KEY (sebo_id) REFERENCES sebo(id),
    CONSTRAINT fk_documento_admin FOREIGN KEY (analisado_por) REFERENCES account(id)
);

CREATE TABLE auditoria_verificacao_sebo (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sebo_id BIGINT NOT NULL,
    analisado_por BIGINT NOT NULL,
    status_anterior VARCHAR(20) NOT NULL,
    novo_status VARCHAR(20) NOT NULL,
    motivo VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_auditoria_sebo FOREIGN KEY (sebo_id) REFERENCES sebo(id),
    CONSTRAINT fk_auditoria_admin FOREIGN KEY (analisado_por) REFERENCES account(id)
);

CREATE TABLE categoria (
    id          BIGINT       NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    slug        VARCHAR(140) NOT NULL,
    descricao   VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_categoria_nome UNIQUE (nome),
    CONSTRAINT uk_categoria_slug UNIQUE (slug)
);

CREATE TABLE catalogo_livro (
    id             BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn           VARCHAR(13) NOT NULL UNIQUE,
    titulo         VARCHAR(255) NOT NULL,
    autores        VARCHAR(500),
    editora        VARCHAR(255),
    edicao         VARCHAR(100),
    ano_publicacao INTEGER,
    idioma         VARCHAR(50),
    capa_url       VARCHAR(500),
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

CREATE TABLE produto (
    id          BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sebo_id     BIGINT         NOT NULL,
    categoria_id BIGINT,
    catalogo_livro_id BIGINT,
    titulo      VARCHAR(255)   NOT NULL,
    autor       VARCHAR(255),
    descricao   VARCHAR(2000),
    preco       NUMERIC(12, 2) NOT NULL,
    estoque     INTEGER        NOT NULL DEFAULT 1,
    condicao    VARCHAR(20)    NOT NULL,
    foto_url    VARCHAR(500),
    ativo       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,
    CONSTRAINT fk_produto_sebo FOREIGN KEY (sebo_id) REFERENCES sebo (id) ON DELETE CASCADE,
    CONSTRAINT fk_produto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT fk_produto_catalogo FOREIGN KEY (catalogo_livro_id) REFERENCES catalogo_livro (id),
    CONSTRAINT ck_produto_condicao CHECK (condicao IN ('NOVO', 'USADO', 'SEMINOVO')),
    CONSTRAINT ck_produto_preco CHECK (preco >= 0),
    CONSTRAINT ck_produto_estoque CHECK (estoque >= 0)
);

CREATE TABLE movimentacao_estoque (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id BIGINT NOT NULL,
    sebo_id BIGINT NOT NULL,
    operador_id BIGINT NOT NULL,
    movimentacao_origem_id BIGINT,
    tipo VARCHAR(20) NOT NULL,
    quantidade INTEGER NOT NULL,
    estoque_antes INTEGER NOT NULL,
    estoque_depois INTEGER NOT NULL,
    valor_unitario NUMERIC(12, 2),
    valor_total NUMERIC(12, 2),
    observacao VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_mov_produto FOREIGN KEY (produto_id) REFERENCES produto(id),
    CONSTRAINT fk_mov_sebo FOREIGN KEY (sebo_id) REFERENCES sebo(id),
    CONSTRAINT fk_mov_operador FOREIGN KEY (operador_id) REFERENCES account(id),
    CONSTRAINT fk_mov_origem FOREIGN KEY (movimentacao_origem_id) REFERENCES movimentacao_estoque(id)
);

CREATE TABLE oferta (
    id                 BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    produto_id         BIGINT         NOT NULL,
    preco_promocional  NUMERIC(12, 2) NOT NULL,
    descricao          VARCHAR(500),
    inicio_em          TIMESTAMP,
    fim_em             TIMESTAMP,
    ativa              BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP      NOT NULL,
    updated_at         TIMESTAMP      NOT NULL,
    CONSTRAINT fk_oferta_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT ck_oferta_preco CHECK (preco_promocional > 0),
    CONSTRAINT ck_oferta_periodo CHECK (fim_em IS NULL OR inicio_em IS NULL OR fim_em >= inicio_em)
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
    quantidade INTEGER   NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_cesta_item UNIQUE (account_id, produto_id),
    CONSTRAINT fk_cesta_account FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_cesta_produto FOREIGN KEY (produto_id) REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT ck_cesta_item_quantidade CHECK (quantidade > 0)
);

CREATE TABLE mensagem (
    id              BIGINT        NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    remetente_id    BIGINT        NOT NULL,
    destinatario_id BIGINT        NOT NULL,
    conteudo        VARCHAR(2000) NOT NULL,
    lida            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (remetente_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_destinatario FOREIGN KEY (destinatario_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT ck_mensagem_destinatario CHECK (remetente_id <> destinatario_id)
);

CREATE TABLE pedido (
    id         BIGINT         NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT         NOT NULL,
    sebo_id    BIGINT         NOT NULL,
    status     VARCHAR(30)    NOT NULL DEFAULT 'AGUARDANDO_CONFIRMACAO',
    forma_pagamento  VARCHAR(20),
    status_pagamento VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    total      NUMERIC(12, 2) NOT NULL,
    confirmado_em TIMESTAMP,
    pago_em TIMESTAMP,
    cancelado_em TIMESTAMP,
    reembolsado_em TIMESTAMP,
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
    quantidade       INTEGER        NOT NULL DEFAULT 1,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    CONSTRAINT fk_pedido_item_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedido (id) ON DELETE CASCADE,
    CONSTRAINT fk_pedido_item_produto FOREIGN KEY (produto_id) REFERENCES produto (id),
    CONSTRAINT ck_pedido_item_quantidade CHECK (quantidade > 0)
);
