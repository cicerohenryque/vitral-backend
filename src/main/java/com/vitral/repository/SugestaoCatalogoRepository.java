package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vitral.entity.SugestaoCatalogo;

import jakarta.persistence.LockModeType;

public interface SugestaoCatalogoRepository extends JpaRepository<SugestaoCatalogo, Long> {

    @EntityGraph(attributePaths = {"usuario", "sebo.account", "categoria", "produtoCriado"})
    Page<SugestaoCatalogo> findByUsuarioId(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "sebo.account", "categoria", "produtoCriado"})
    Page<SugestaoCatalogo> findBySeboAccountId(Long accountId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SugestaoCatalogo s join fetch s.sebo sebo join fetch sebo.account where s.id = :id")
    Optional<SugestaoCatalogo> findByIdParaDecisao(@Param("id") Long id);
}
