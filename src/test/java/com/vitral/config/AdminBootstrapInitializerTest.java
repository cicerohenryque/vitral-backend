package com.vitral.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapInitializerTest {
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void criaAdminCompletoSomenteQuandoConfigurado() throws Exception {
        AdminBootstrapProperties properties = new AdminBootstrapProperties(
                "ADMIN@VITRAL.COM", "senha-segura-123", "Admin Vitral");
        when(accountRepository.existsByType(AccountType.ADMIN)).thenReturn(false);
        when(accountRepository.existsByEmail("admin@vitral.com")).thenReturn(false);
        when(accountRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("senha-segura-123")).thenReturn("hash");

        new AdminBootstrapInitializer(properties, accountRepository, passwordEncoder).run(null);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin");
        assertThat(captor.getValue().getEmail()).isEqualTo("admin@vitral.com");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash");
        assertThat(captor.getValue().getType()).isEqualTo(AccountType.ADMIN);
        assertThat(captor.getValue().isEmailVerificado()).isTrue();
        assertThat(captor.getValue().getAtivo()).isTrue();
    }
}
