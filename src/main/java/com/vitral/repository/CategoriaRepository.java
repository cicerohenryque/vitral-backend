package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vitral.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
