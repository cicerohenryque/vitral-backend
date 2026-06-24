package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.vitral.dto.SeboRequest;
import com.vitral.dto.SeboResponse;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class SeboServiceTest {

    @Mock
    private SeboRepository seboRepository;

    @Mock
    private SeboMapper seboMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private SeboService seboService;

    @Captor
    private ArgumentCaptor<Sebo> seboCaptor;

    private Account contaSebo;
    private Account contaUsuario;
    private SeboRequest request;

    @BeforeEach
    void setUp() {
        contaSebo = Account.builder()
                .name("Sebo do Joao")
                .email("sebo@vitral.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .build();
        setAccountId(contaSebo, 10L);

        contaUsuario = Account.builder()
                .name("Maria")
                .email("maria@vitral.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .build();
        setAccountId(contaUsuario, 20L);

        request = new SeboRequest(
                "Sebo familiar com livros raros",
                "8399999-0000",
                "58400-000",
                "Rua das Flores, 123",
                "Campina Grande",
                "PB",
                "Seg-Sex 09:00-18:00",
                "https://cdn/foto.jpg");
    }

    @Test
    @DisplayName("Deve criar sebo para conta do tipo SEBO ainda sem perfil cadastrado")
    void shouldCreateSeboWhenAccountIsSeboAndHasNoProfileYet() {
        SeboResponse responseEsperado = new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cep(), request.logradouro(),
                request.cidade(), request.uf(), request.horarioFuncionamento(), request.fotoUrl());

        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.save(seboCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(seboMapper.toResponse(seboCaptor.capture())).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.criar(contaSebo, request);

        assertThat(resultado).isEqualTo(responseEsperado);
        Sebo persistido = seboCaptor.getAllValues().get(0);
        assertThat(persistido.getAccount()).isSameAs(contaSebo);
        assertThat(persistido.getDescricao()).isEqualTo(request.descricao());
        assertThat(persistido.getTelefone()).isEqualTo(request.telefone());
        assertThat(persistido.getCidade()).isEqualTo("Campina Grande");
        assertThat(persistido.getUf()).isEqualTo("PB");
        assertThat(persistido.getHorarioFuncionamento()).isEqualTo("Seg-Sex 09:00-18:00");
    }

    @Test
    @DisplayName("Deve lancar BusinessException com status 403 quando conta nao for do tipo SEBO")
    void shouldRejectWhenAccountTypeIsNotSebo() {
        assertThatThrownBy(() -> seboService.criar(contaUsuario, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SEBO")
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        verify(seboRepository, never()).save(seboCaptor.capture());
    }

    @Test
    @DisplayName("Deve lancar ConflictException quando ja existir sebo cadastrado para a conta")
    void shouldThrowConflictWhenSeboAlreadyExistsForAccount() {
        when(seboRepository.existsByAccountId(10L)).thenReturn(true);

        assertThatThrownBy(() -> seboService.criar(contaSebo, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Sebo ja cadastrado para esta conta");

        verify(seboRepository, never()).save(seboCaptor.capture());
    }

    @Test
    @DisplayName("Deve atualizar dados do sebo existente vinculado a conta autenticada")
    void shouldUpdateExistingSeboLinkedToAccount() {
        Sebo existente = Sebo.builder()
                .account(contaSebo)
                .descricao("Antiga")
                .cidade("Joao Pessoa")
                .uf("PB")
                .build();
        SeboResponse responseEsperado = new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cep(), request.logradouro(),
                request.cidade(), request.uf(), request.horarioFuncionamento(), request.fotoUrl());

        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.of(existente));
        when(seboMapper.toResponse(existente)).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.atualizarMeuSebo(contaSebo, request);

        assertThat(resultado).isEqualTo(responseEsperado);
        assertThat(existente.getDescricao()).isEqualTo(request.descricao());
        assertThat(existente.getCidade()).isEqualTo("Campina Grande");
        assertThat(existente.getTelefone()).isEqualTo(request.telefone());
        assertThat(existente.getFotoUrl()).isEqualTo(request.fotoUrl());
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao atualizar quando a conta nao possui sebo")
    void shouldThrowNotFoundWhenUpdatingMissingSebo() {
        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seboService.atualizarMeuSebo(contaSebo, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sebo nao encontrado para esta conta");
    }

    @Test
    @DisplayName("Deve retornar resposta mapeada ao buscar sebo por id existente")
    void shouldReturnSeboWhenIdExists() {
        Sebo sebo = Sebo.builder().account(contaSebo).cidade("Recife").build();
        SeboResponse responseEsperado = new SeboResponse(7L, 10L, "Sebo do Joao", "sebo@vitral.com",
                null, null, null, null, "Recife", null, null, null);

        when(seboRepository.findById(7L)).thenReturn(Optional.of(sebo));
        when(seboMapper.toResponse(sebo)).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.buscarPorId(7L);

        assertThat(resultado).isEqualTo(responseEsperado);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar sebo por id inexistente")
    void shouldThrowNotFoundWhenSeboIdDoesNotExist() {
        when(seboRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seboService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sebo nao encontrado");
    }

    @Test
    @DisplayName("Deve recuperar entidade Sebo a partir do id da conta autenticada")
    void shouldReturnSeboEntityByAccountId() {
        Sebo sebo = Sebo.builder().account(contaSebo).build();
        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.of(sebo));

        Sebo resultado = seboService.buscarEntidadePorAccount(10L);

        assertThat(resultado).isSameAs(sebo);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao recuperar entidade Sebo sem perfil cadastrado")
    void shouldThrowNotFoundWhenAccountHasNoSebo() {
        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seboService.buscarEntidadePorAccount(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sebo nao encontrado para esta conta");
    }

    @Test
    @DisplayName("Deve excluir conta do sebo autenticado")
    void excluirConta_deletaAccount() {
        MensagemResponse response = seboService.excluirConta(contaSebo);

        verify(accountRepository).delete(contaSebo);
        assertThat(response.mensagem()).isEqualTo("Conta do sebo excluida com sucesso");
    }

    private void setAccountId(Account account, Long id) {
        try {
            java.lang.reflect.Field field = com.vitral.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
