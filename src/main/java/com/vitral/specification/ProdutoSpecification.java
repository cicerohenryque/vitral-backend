package com.vitral.specification;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Produto;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.BookGenre;

public class ProdutoSpecification {
    private static final String CARACTERES_ACENTUADOS = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String CARACTERES_NORMALIZADOS = "aaaaaeeeeiiiiooooouuuuc";

    private ProdutoSpecification() {
    }

    public static Specification<Produto> ativo() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.get("ativo")),
                cb.isTrue(root.get("sebo").get("account").get("ativo")),
                cb.equal(root.get("sebo").get("statusVerificacao"),
                        com.vitral.enumerations.StatusVerificacaoSebo.VERIFICADO));
    }

    public static Specification<Produto> disponivel() {
        return ativo().and((root, query, cb) -> cb.greaterThan(root.get("estoque"), 0));
    }

    public static Specification<Produto> ativoIgual(Boolean ativo) {
        return (root, query, cb) -> ativo == null ? cb.conjunction() : cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<Produto> classico() {
        return (root, query, cb) -> cb.isTrue(root.get("classico"));
    }

    public static Specification<Produto> naoClassico() {
        return (root, query, cb) -> cb.isFalse(root.get("classico"));
    }

    public static Specification<Produto> lancamentoOuRecente(OffsetDateTime desde) {
        return (root, query, cb) -> cb.or(cb.isTrue(root.get("lancamento")),
                cb.greaterThanOrEqualTo(root.get("createdAt"), desde));
    }

    public static Specification<Produto> idsFora(Collection<Long> ids) {
        return (root, query, cb) -> ids == null || ids.isEmpty() ? cb.conjunction() : cb.not(root.get("id").in(ids));
    }

    public static Specification<Produto> tituloContem(String termo) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("titulo")),
                "%" + termo.toLowerCase() + "%");
    }

    public static Specification<Produto> tituloOuAutorContemAlgum(Collection<String> termos) {
        return (root, query, cb) -> {
            List<String> validos = termos == null ? List.of() : termos.stream()
                    .filter(termo -> termo != null && !termo.isBlank())
                    .map(ProdutoSpecification::normalizarTermo)
                    .filter(termo -> !termo.isBlank())
                    .toList();
            if (validos.isEmpty()) return cb.disjunction();
            return cb.or(validos.stream()
                    .flatMap(termo -> java.util.stream.Stream.of(
                            cb.like(semAcentos(cb, root.get("titulo")), "%" + termo + "%"),
                            cb.like(semAcentos(cb, root.get("autor")), "%" + termo + "%")))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    public static Specification<Produto> tituloOuAutorContem(String termo) {
        List<String> palavras = palavrasNormalizadas(termo);
        return (root, query, cb) -> {
            if (palavras.isEmpty()) return cb.disjunction();
            var titulo = semAcentos(cb, root.get("titulo"));
            var autor = semAcentos(cb, root.get("autor"));
            return cb.and(palavras.stream()
                    .map(palavra -> cb.or(
                            cb.like(titulo, "%" + palavra + "%"),
                            cb.like(autor, "%" + palavra + "%")))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private static jakarta.persistence.criteria.Expression<String> semAcentos(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String> campo) {
        return cb.function("translate", String.class, cb.lower(campo),
                cb.literal(CARACTERES_ACENTUADOS), cb.literal(CARACTERES_NORMALIZADOS));
    }

    private static String normalizarTermo(String termo) {
        return Normalizer.normalize(termo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static List<String> palavrasNormalizadas(String termo) {
        String normalizado = termo == null ? "" : normalizarTermo(termo);
        return normalizado.isBlank() ? List.of() : List.of(normalizado.split(" "));
    }

    public static Specification<Produto> doSebo(Long seboId) {
        return (root, query, cb) -> seboId == null ? cb.conjunction() : cb.equal(root.get("sebo").get("id"), seboId);
    }

    public static Specification<Produto> daCategoria(Long categoriaId) {
        return (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Produto> dasCategorias(Collection<Long> categoriaIds) {
        return (root, query, cb) -> categoriaIds == null || categoriaIds.isEmpty()
                ? cb.disjunction()
                : root.get("categoria").get("id").in(categoriaIds);
    }

    public static Specification<Produto> comBookGenre(BookGenre bookGenre) {
        return (root, query, cb) -> cb.equal(root.get("bookGenre"), bookGenre);
    }

    public static Specification<Produto> comCondicao(CondicaoProduto condicao) {
        return (root, query, cb) -> cb.equal(root.get("condicao"), condicao);
    }

    public static Specification<Produto> precoMinimo(BigDecimal precoMin) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precoEfetivo"), precoMin);
    }

    public static Specification<Produto> precoMaximo(BigDecimal precoMax) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("precoEfetivo"), precoMax);
    }
}
