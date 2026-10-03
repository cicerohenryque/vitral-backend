package com.vitral.specification;

import java.text.Normalizer;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Sebo;

import jakarta.persistence.criteria.JoinType;

public class SeboSpecification {
    private static final String CARACTERES_ACENTUADOS = "áàâãäéèêëíìîïóòôõöúùûüç";
    private static final String CARACTERES_NORMALIZADOS = "aaaaaeeeeiiiiooooouuuuc";

    private SeboSpecification() {
    }

    public static Specification<Sebo> ativo() {
        return (root, query, cb) -> cb.and(
                cb.isTrue(root.join("account", JoinType.INNER).get("ativo")),
                cb.equal(root.get("statusVerificacao"),
                        com.vitral.enumerations.StatusVerificacaoSebo.VERIFICADO));
    }

    public static Specification<Sebo> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Sebo> statusIgual(com.vitral.enumerations.StatusVerificacaoSebo status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("statusVerificacao"), status);
    }

    public static Specification<Sebo> nomeContem(String termo) {
        List<String> palavras = palavrasNormalizadas(termo);
        return (root, query, cb) -> {
            if (palavras.isEmpty()) return cb.disjunction();
            var nome = semAcentos(cb, root.join("account", JoinType.INNER).get("name"));
            return cb.and(palavras.stream().map(palavra -> cb.like(nome, "%" + palavra + "%"))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    public static Specification<Sebo> cidadeContem(String cidade) {
        List<String> palavras = palavrasNormalizadas(cidade);
        return (root, query, cb) -> {
            if (palavras.isEmpty()) return cb.disjunction();
            var nomeCidade = semAcentos(cb, root.get("cidade"));
            return cb.and(palavras.stream().map(palavra -> cb.like(nomeCidade, "%" + palavra + "%"))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    public static Specification<Sebo> ufIgual(String uf) {
        return (root, query, cb) -> cb.equal(root.get("uf"), uf.trim().toUpperCase());
    }

    public static Specification<Sebo> dentroDoRetangulo(double latitudeMinima, double latitudeMaxima,
            double longitudeMinima, double longitudeMaxima) {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("latitude")),
                cb.isNotNull(root.get("longitude")),
                cb.between(root.get("latitude"), latitudeMinima, latitudeMaxima),
                cb.between(root.get("longitude"), longitudeMinima, longitudeMaxima));
    }

    private static jakarta.persistence.criteria.Expression<String> semAcentos(
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Expression<String> campo) {
        return cb.function("translate", String.class, cb.lower(campo),
                cb.literal(CARACTERES_ACENTUADOS), cb.literal(CARACTERES_NORMALIZADOS));
    }

    private static String normalizarTermo(String termo) {
        if (termo == null) return "";
        return Normalizer.normalize(termo.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static List<String> palavrasNormalizadas(String termo) {
        String normalizado = normalizarTermo(termo);
        return normalizado.isBlank() ? List.of() : List.of(normalizado.split(" "));
    }

}
