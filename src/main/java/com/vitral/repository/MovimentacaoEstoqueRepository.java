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
            select month(m.createdAt), coalesce(sum(m.valorTotal), 0)
            from MovimentacaoEstoque m
            where m.sebo.id = :seboId
              and year(m.createdAt) = :ano
              and m.tipo = com.vitral.enumerations.TipoMovimentacaoEstoque.VENDA_FISICA
            group by month(m.createdAt)
            order by month(m.createdAt)
            """)
    List<Object[]> vendasFisicasMensais(@Param("seboId") Long seboId, @Param("ano") int ano);

    @Query("""
            select month(m.createdAt), coalesce(sum(m.valorTotal), 0)
            from MovimentacaoEstoque m
            where m.sebo.id = :seboId
              and year(m.createdAt) = :ano
              and m.tipo = com.vitral.enumerations.TipoMovimentacaoEstoque.ESTORNO
              and m.movimentacaoOrigem.tipo = com.vitral.enumerations.TipoMovimentacaoEstoque.VENDA_FISICA
            group by month(m.createdAt)
            order by month(m.createdAt)
            """)
    List<Object[]> estornosFisicosMensais(@Param("seboId") Long seboId, @Param("ano") int ano);
}
