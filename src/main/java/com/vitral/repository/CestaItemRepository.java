package com.vitral.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vitral.entity.CestaItem;

@Repository
public interface CestaItemRepository extends JpaRepository<CestaItem, Long> {

    List<CestaItem> findByAccountId(Long accountId);

    Optional<CestaItem> findByAccountIdAndProdutoId(Long accountId, Long produtoId);

    boolean existsByAccountIdAndProdutoId(Long accountId, Long produtoId);

    void deleteByAccountId(Long accountId);
}
