package com.vitral.specification;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Sebo;

import jakarta.persistence.criteria.JoinType;

public class SeboSpecification {

    private SeboSpecification() {
    }

    public static Specification<Sebo> ativo() {
        return (root, query, cb) -> cb.isTrue(root.join("account", JoinType.INNER).get("ativo"));
    }

    public static Specification<Sebo> nomeContem(String termo) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.join("account", JoinType.INNER).get("name")),
                "%" + termo.toLowerCase() + "%");
    }

    public static Specification<Sebo> daCidade(String cidade) {
        return (root, query, cb) -> cb.equal(
                cb.lower(root.get("cidade")),
                cidade.toLowerCase());
    }

    public static Specification<Sebo> doEstado(String uf) {
        return (root, query, cb) -> cb.equal(
                cb.upper(root.get("uf")),
                uf.toUpperCase());
    }
}
