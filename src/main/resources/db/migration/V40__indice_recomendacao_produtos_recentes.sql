-- Sustenta o fallback de recomendacoes, que filtra produtos disponiveis e ordena pelos mais recentes.
-- Categoria e sebo ja possuem indices proprios; favoritos e cesta possuem chaves unicas iniciadas por account_id.
CREATE INDEX ix_produto_recomendacao_recente
    ON produto (created_at DESC, id)
    WHERE ativo = TRUE AND estoque > 0;
