CREATE TABLE categoria (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    slug       VARCHAR(100) NOT NULL UNIQUE,
    descricao  VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

ALTER TABLE produto ADD COLUMN categoria_id BIGINT REFERENCES categoria(id);

INSERT INTO categoria (nome, slug, descricao) VALUES
    ('Literatura Brasileira', 'literatura-brasileira', 'Autores e obras nacionais'),
    ('Ficção', 'ficcao', 'Romances, contos e narrativas ficcionais'),
    ('Não-ficção', 'nao-ficcao', 'Biografias, memórias e relatos reais'),
    ('Ciência e Tecnologia', 'ciencia-tecnologia', 'Obras científicas e técnicas'),
    ('História', 'historia', 'Obras históricas e documentais'),
    ('Filosofia', 'filosofia', 'Filosofia, ética e pensamento'),
    ('Infantil e Juvenil', 'infantil-juvenil', 'Livros para crianças e jovens'),
    ('Auto-ajuda', 'auto-ajuda', 'Desenvolvimento pessoal e bem-estar'),
    ('HQs e Mangás', 'hqs-mangas', 'Quadrinhos, graphic novels e mangás'),
    ('Gastronomia', 'gastronomia', 'Culinária e cultura gastronômica');
