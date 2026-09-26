package com.vitral.specification;

import java.time.OffsetDateTime;
import org.springframework.data.jpa.domain.Specification;
import com.vitral.entity.AdminAudit;

public final class AdminAuditSpecification {
    private AdminAuditSpecification() {}

    public static Specification<AdminAudit> filtros(Long adminId, String acao, String entidade,
            OffsetDateTime inicio, OffsetDateTime fim) {
        return (root, query, cb) -> cb.and(
                adminId == null ? cb.conjunction() : cb.equal(root.get("admin").get("id"), adminId),
                vazio(acao) ? cb.conjunction() : cb.equal(root.get("acao"), acao.trim().toUpperCase()),
                vazio(entidade) ? cb.conjunction() : cb.equal(root.get("entidade"), entidade.trim().toUpperCase()),
                inicio == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), inicio),
                fim == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), fim));
    }

    private static boolean vazio(String valor) { return valor == null || valor.isBlank(); }
}
