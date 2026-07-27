package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;

import com.vitral.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
            select c from Categoria c where c.slug in :slugs
            order by case c.slug when 'livros' then 1 when 'cds' then 2 when 'vinis' then 3 when 'hqs-mangas' then 4 else 5 end
            """)
    Page<Categoria> findPermitidas(Collection<String> slugs, Pageable pageable);
}
