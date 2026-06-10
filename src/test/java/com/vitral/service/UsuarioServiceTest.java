package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AtualizarPerfilRequest;
import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.mapper.AccountMapper;
import com.vitral.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Test
    @DisplayName("Deve atualizar o nome do perfil e retornar os dados atualizados")
    void shouldUpdateProfileNameAndReturnUpdatedResponse() {
        Account account = Account.builder()
                .name("Maria")
                .email("maria@vitral.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .emailVerificado(true)
                .build();
        AtualizarPerfilRequest request = new AtualizarPerfilRequest("Maria Clara");
        AccountResponse expectedResponse = new AccountResponse(1L, "Maria Clara", "maria@vitral.com", AccountType.USUARIO);

        when(accountRepository.save(accountCaptor.capture())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(expectedResponse);

        AccountResponse result = usuarioService.atualizarPerfil(account, request);

        assertThat(result.name()).isEqualTo("Maria Clara");
        assertThat(accountCaptor.getValue().getName()).isEqualTo("Maria Clara");
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("Deve persistir exatamente o nome informado na requisicao sem alteracoes")
    void shouldPersistExactlyTheNameFromRequest() {
        Account account = Account.builder()
                .name("Nome Antigo")
                .email("sebo@vitral.com")
                .type(AccountType.SEBO)
                .emailVerificado(true)
                .build();
        AtualizarPerfilRequest request = new AtualizarPerfilRequest("Livraria Novo Nome");

        when(accountRepository.save(accountCaptor.capture())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(
                new AccountResponse(2L, "Livraria Novo Nome", "sebo@vitral.com", AccountType.SEBO));

        usuarioService.atualizarPerfil(account, request);

        assertThat(accountCaptor.getValue().getName()).isEqualTo("Livraria Novo Nome");
        assertThat(accountCaptor.getValue().getEmail()).isEqualTo("sebo@vitral.com");
    }
}
