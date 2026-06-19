package com.vitral.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vitral.entity.Favorito;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    List<Favorito> findByAccountId(Long accountId);

    Optional<Favorito> findByAccountIdAndProdutoId(Long accountId, Long produtoId);

    boolean existsByAccountIdAndProdutoId(Long accountId, Long produtoId);
}
