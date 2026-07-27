package com.vitral.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Produto;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.BookGenre;

public class ProdutoSpecification {

    private ProdutoSpecification() {
    }

    public static Specification<Produto> ativo() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("ativo")),
                cb.isTrue(root.get("sebo").get("account").get("ativo")),
                cb.equal(root.get("sebo").get("statusVerificacao"),
                        com.vitral.enumerations.StatusVerificacaoSebo.VERIFICADO));
    }

    public static Specification<Produto> tituloContem(String termo) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("titulo")),
                "%" + termo.toLowerCase() + "%");
    }

    public static Specification<Produto> doSebo(Long seboId) {
        return (root, query, cb) -> cb.equal(root.get("sebo").get("id"), seboId);
    }

    public static Specification<Produto> daCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Produto> comBookGenre(BookGenre bookGenre) {
        return (root, query, cb) -> cb.equal(root.get("bookGenre"), bookGenre);
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
