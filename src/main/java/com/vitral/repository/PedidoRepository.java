package com.vitral.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vitral.entity.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Histórico de pedidos do usuário (paginado)
    Page<Pedido> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // Histórico de vendas do sebo (paginado)
    Page<Pedido> findBySeboIdOrderByCreatedAtDesc(Long seboId, Pageable pageable);

    // Para verificar se há pedido ativo com aquele produto (evitar duplicidade)
    List<Pedido> findByAccountId(Long accountId);
}
