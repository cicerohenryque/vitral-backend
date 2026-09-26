package com.vitral.specification;

import org.springframework.data.jpa.domain.Specification;

import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;

public final class AccountSpecification {
    private AccountSpecification() {}

    public static Specification<Account> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Account> termo(String termo) {
        return (root, query, cb) -> {
            if (termo == null || termo.isBlank()) return cb.conjunction();
            String like = "%" + termo.trim().toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("email")), like));
        };
    }

    public static Specification<Account> tipo(AccountType tipo) {
        return (root, query, cb) -> tipo == null ? cb.conjunction() : cb.equal(root.get("type"), tipo);
    }

    public static Specification<Account> ativo(Boolean ativo) {
        return (root, query, cb) -> ativo == null ? cb.conjunction() : cb.equal(root.get("ativo"), ativo);
    }
}
