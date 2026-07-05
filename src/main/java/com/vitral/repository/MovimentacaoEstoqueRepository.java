package com.vitral.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vitral.entity.MovimentacaoEstoque;
import com.vitral.enumerations.TipoMovimentacaoEstoque;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    boolean existsByMovimentacaoOrigemIdAndTipo(Long origemId, TipoMovimentacaoEstoque tipo);
    Page<MovimentacaoEstoque> findBySeboAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("""
            select month(m.createdAt),
                   coalesce(sum(case when m.tipo = com.vitral.enumerations.TipoMovimentacaoEstoque.VENDA_FISICA
                       then m.valorTotal else -m.valorTotal end), 0)
            from MovimentacaoEstoque m
            where m.sebo.id = :seboId
              and year(m.createdAt) = :ano
              and m.tipo in (com.vitral.enumerations.TipoMovimentacaoEstoque.VENDA_FISICA,
                             com.vitral.enumerations.TipoMovimentacaoEstoque.ESTORNO)
              and (m.tipo <> com.vitral.enumerations.TipoMovimentacaoEstoque.ESTORNO
                   or m.movimentacaoOrigem.tipo = com.vitral.enumerations.TipoMovimentacaoEstoque.VENDA_FISICA)
            group by month(m.createdAt)
            order by month(m.createdAt)
            """)
    List<Object[]> faturamentoFisicoMensal(@Param("seboId") Long seboId, @Param("ano") int ano);
}
