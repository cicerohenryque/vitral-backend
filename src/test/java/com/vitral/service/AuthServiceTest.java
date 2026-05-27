package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AuthResponse;
import com.vitral.dto.RegisterRequest;
import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.exception.ConflictException;
import com.vitral.mapper.AccountMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Test
    @DisplayName("Deve registrar nova conta e retornar token quando o email ainda nao existe")
    void shouldRegisterAccountWhenEmailIsAvailable() {
        RegisterRequest request = new RegisterRequest("Maria", "maria@vitral.com", "senha1234", AccountType.USUARIO);
        Account savedAccount = Account.builder()
                .name("Maria")
                .email("maria@vitral.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .build();
        AccountResponse accountResponse = new AccountResponse(1L, "Maria", "maria@vitral.com", AccountType.USUARIO);

        when(accountRepository.existsByEmail("maria@vitral.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash");
        when(accountRepository.save(accountCaptor.capture())).thenReturn(savedAccount);
        when(jwtService.generateToken("maria@vitral.com", "USUARIO")).thenReturn("token-jwt");
        when(jwtService.getExpiration()).thenReturn(86400000L);
        when(accountMapper.toResponse(savedAccount)).thenReturn(accountResponse);

        AuthResponse result = authService.register(request);

        assertThat(result.token()).isEqualTo("token-jwt");
        assertThat(result.expiresIn()).isEqualTo(86400000L);
        assertThat(result.account()).isEqualTo(accountResponse);

        Account persisted = accountCaptor.getValue();
        assertThat(persisted.getName()).isEqualTo("Maria");
        assertThat(persisted.getEmail()).isEqualTo("maria@vitral.com");
        assertThat(persisted.getPasswordHash()).isEqualTo("hash");
        assertThat(persisted.getType()).isEqualTo(AccountType.USUARIO);
    }

    @Test
    @DisplayName("Deve lancar ConflictException e nao salvar quando o email ja esta cadastrado")
    void shouldThrowConflictWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Maria", "maria@vitral.com", "senha1234", AccountType.USUARIO);

        when(accountRepository.existsByEmail("maria@vitral.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email ja cadastrado");
        verify(accountRepository, never()).save(any(Account.class));
    }
}
