package com.vitral.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminBootstrapInitializer implements ApplicationRunner {
    private final AdminBootstrapProperties properties;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (blank(properties.email()) || blank(properties.password()) || accountRepository.existsByType(AccountType.ADMIN)) return;
        if (properties.password().length() < 12) throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD deve ter ao menos 12 caracteres");
        String email = properties.email().trim().toLowerCase();
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_EMAIL já pertence a outra conta");
        }
        String username = accountRepository.existsByUsername("admin")
                ? "admin-" + Integer.toUnsignedString(email.hashCode(), 36)
                : "admin";
        accountRepository.save(Account.builder().name(blank(properties.name()) ? "Administrador" : properties.name().trim())
                .username(username).email(email).passwordHash(passwordEncoder.encode(properties.password()))
                .type(AccountType.ADMIN).emailVerificado(true).ativo(true).build());
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }
}
