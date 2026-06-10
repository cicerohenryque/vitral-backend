package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vitral.entity.Sebo;

public interface SeboRepository extends JpaRepository<Sebo, Long>, JpaSpecificationExecutor<Sebo> {

    Optional<Sebo> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    Page<Sebo> findByAccountNameContainingIgnoreCase(String termo, Pageable pageable);

    Page<Sebo> findByAccountNameContainingIgnoreCaseAndCidadeIgnoreCase(String termo, String cidade, Pageable pageable);

    Page<Sebo> findByCidadeIgnoreCase(String cidade, Pageable pageable);
}
