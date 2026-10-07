package com.vitral.specification;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;
import com.vitral.entity.Pedido;
import com.vitral.enumerations.StatusPedido;

public final class PedidoSpecification {
    private PedidoSpecification() {}

    public static Specification<Pedido> filtros(Long id, StatusPedido status, OffsetDateTime inicio, OffsetDateTime fim) {
        return (root, query, cb) -> cb.and(
                id == null ? cb.conjunction() : cb.equal(root.get("id"), id),
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status),
                inicio == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), inicio),
                fim == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), fim));
    }
}
