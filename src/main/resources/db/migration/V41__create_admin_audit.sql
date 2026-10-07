CREATE TABLE admin_audit (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES account(id),
    acao VARCHAR(60) NOT NULL,
    entidade VARCHAR(60) NOT NULL,
    entidade_id BIGINT NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_admin_audit_created_at ON admin_audit (created_at DESC);
CREATE INDEX idx_admin_audit_admin_created_at ON admin_audit (admin_id, created_at DESC);
