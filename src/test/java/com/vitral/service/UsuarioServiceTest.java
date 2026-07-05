package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AtualizarPerfilRequest;
import com.vitral.dto.MensagemResponse;
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

    @Mock
    private AnonimizacaoContaService anonimizacaoContaService;

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
        AtualizarPerfilRequest request = new AtualizarPerfilRequest("Maria Clara", "/api/v1/uploads/images/perfil.jpg");
        AccountResponse expectedResponse = new AccountResponse(1L, "Maria Clara", "maria@vitral.com", AccountType.USUARIO, "/api/v1/uploads/images/perfil.jpg");

        when(accountRepository.save(accountCaptor.capture())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(expectedResponse);

        AccountResponse result = usuarioService.atualizarPerfil(account, request);

        assertThat(result.name()).isEqualTo("Maria Clara");
        assertThat(accountCaptor.getValue().getName()).isEqualTo("Maria Clara");
        assertThat(accountCaptor.getValue().getFotoUrl()).isEqualTo("/api/v1/uploads/images/perfil.jpg");
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
        AtualizarPerfilRequest request = new AtualizarPerfilRequest("Livraria Novo Nome", null);

        when(accountRepository.save(accountCaptor.capture())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(
                new AccountResponse(2L, "Livraria Novo Nome", "sebo@vitral.com", AccountType.SEBO, null));

        usuarioService.atualizarPerfil(account, request);

        assertThat(accountCaptor.getValue().getName()).isEqualTo("Livraria Novo Nome");
        assertThat(accountCaptor.getValue().getEmail()).isEqualTo("sebo@vitral.com");
    }

    @Test
    @DisplayName("Deve anonimizar e desativar conta do usuario autenticado")
    void excluirConta_desativaAccount() {
        Account account = Account.builder()
                .name("Maria")
                .email("maria@vitral.com")
                .type(AccountType.USUARIO)
                .build();

        MensagemResponse response = usuarioService.excluirConta(account);

        verify(anonimizacaoContaService).anonimizarUsuario(account);
        verify(accountRepository, never()).delete(account);
        assertThat(response.mensagem()).isEqualTo("Conta excluida com sucesso");
    }
}
