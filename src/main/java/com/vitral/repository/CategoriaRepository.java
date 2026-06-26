package com.vitral.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vitral.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
