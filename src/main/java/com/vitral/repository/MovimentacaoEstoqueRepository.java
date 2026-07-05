package com.vitral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vitral.entity.MovimentacaoEstoque;
import com.vitral.enumerations.TipoMovimentacaoEstoque;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    boolean existsByMovimentacaoOrigemIdAndTipo(Long origemId, TipoMovimentacaoEstoque tipo);
    Page<MovimentacaoEstoque> findBySeboAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
