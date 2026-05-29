package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.vitral.entity.Sebo;

public interface SeboRepository extends JpaRepository<Sebo, Long> {

    Optional<Sebo> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);

    Page<Sebo> findByAccountNameContainingIgnoreCase(String termo, Pageable pageable);

    Page<Sebo> findByAccountNameContainingIgnoreCaseAndCidadeIgnoreCase(String termo, String cidade, Pageable pageable);

    Page<Sebo> findByCidadeIgnoreCase(String cidade, Pageable pageable);
}
