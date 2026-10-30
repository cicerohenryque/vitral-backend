ALTER TABLE sebo
    ADD COLUMN tipo_catalogo VARCHAR(30) NOT NULL DEFAULT 'SEBO';

ALTER TABLE sebo
    ADD CONSTRAINT ck_sebo_tipo_catalogo
    CHECK (tipo_catalogo IN ('SEBO', 'VENDEDOR_INDEPENDENTE'));

CREATE INDEX ix_sebo_tipo_catalogo_status
    ON sebo (tipo_catalogo, status_verificacao);
