package com.vitral.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.vitral.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndAtivoTrue(String email);

    Optional<Account> findByUsernameAndAtivoTrue(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByType(com.vitral.enumerations.AccountType type);

    long countByType(com.vitral.enumerations.AccountType type);

    long countByTypeAndAtivoTrue(com.vitral.enumerations.AccountType type);
}
