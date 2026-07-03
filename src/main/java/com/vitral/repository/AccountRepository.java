package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vitral.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndAtivoTrue(String email);

    Optional<Account> findFirstByNameOrderByIdAsc(String name);

    Optional<Account> findFirstByNameAndAtivoTrueOrderByIdAsc(String name);

    boolean existsByEmail(String email);

    boolean existsByName(String name);
}
