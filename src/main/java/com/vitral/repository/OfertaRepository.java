package com.vitral.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vitral.entity.Oferta;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    @Query(value = """
            select o from Oferta o
            join fetch o.produto p
            join fetch p.sebo s
            where o.ativa = true
              and p.ativo = true
              and (o.inicioEm is null or o.inicioEm <= :agora)
              and (o.fimEm is null or o.fimEm >= :agora)
            """,
            countQuery = """
            select count(o) from Oferta o
            join o.produto p
            where o.ativa = true
              and p.ativo = true
              and (o.inicioEm is null or o.inicioEm <= :agora)
              and (o.fimEm is null or o.fimEm >= :agora)
            """)
    Page<Oferta> findOfertasAtivas(@Param("agora") OffsetDateTime agora, Pageable pageable);

    Optional<Oferta> findByProdutoIdAndAtivaTrue(Long produtoId);
}
