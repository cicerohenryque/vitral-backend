package com.vitral.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Produto;
import com.vitral.enumerations.CondicaoProduto;

public class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    public static Specification<Produto> ativo() {
        return (root, query, cb) -> cb.isTrue(root.get("ativo"));
    }

    public static Specification<Produto> tituloContem(String termo) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("titulo")),
                "%" + termo.toLowerCase() + "%");
    }

    public static Specification<Produto> doSebo(Long seboId) {
        return (root, query, cb) -> cb.equal(root.get("sebo").get("id"), seboId);
    }

    public static Specification<Produto> comCondicao(CondicaoProduto condicao) {
        return (root, query, cb) -> cb.equal(root.get("condicao"), condicao);
    }

    public static Specification<Produto> precoMinimo(BigDecimal precoMin) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("preco"), precoMin);
    }

    public static Specification<Produto> precoMaximo(BigDecimal precoMax) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("preco"), precoMax);
    }
}
