package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vitral.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    Optional<Produto> findByIdAndAtivoTrue(Long id);

    Page<Produto> findBySeboIdAndAtivoTrue(Long seboId, Pageable pageable);

    Page<Produto> findByAtivoTrue(Pageable pageable);

    Page<Produto> findByAtivoTrueAndTituloContainingIgnoreCase(String termo, Pageable pageable);

    Page<Produto> findByCategoriaIdAndAtivoTrue(Long categoriaId, Pageable pageable);
}
