package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vitral.entity.Oferta;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    Page<Oferta> findByAtivaTrue(Pageable pageable);

    Optional<Oferta> findByIdAndProdutoSeboAccountId(Long id, Long accountId);
}
