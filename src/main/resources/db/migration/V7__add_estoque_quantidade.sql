ALTER TABLE produto
    ADD COLUMN estoque INTEGER NOT NULL DEFAULT 1 CHECK (estoque >= 0);

ALTER TABLE cesta_item
    ADD COLUMN quantidade INTEGER NOT NULL DEFAULT 1 CHECK (quantidade >= 1);

ALTER TABLE pedido_item
    ADD COLUMN quantidade INTEGER NOT NULL DEFAULT 1 CHECK (quantidade >= 1);
