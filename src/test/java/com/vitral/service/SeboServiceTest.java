package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.enumerations.TipoOperacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class SeboServiceTest {

    @Mock
    private SeboRepository seboRepository;

    @Mock
    private SeboMapper seboMapper;

    @Mock
    private AnonimizacaoContaService anonimizacaoContaService;

    @Mock
    private CnpjConsultaService cnpjConsultaService;

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
                "12345678000199",
                "https://cdn/foto.jpg",
                null);
    }

    @Test
    @DisplayName("Deve criar sebo para conta do tipo SEBO ainda sem perfil cadastrado")
    void shouldCreateSeboWhenAccountIsSeboAndHasNoProfileYet() {
        SeboResponse responseEsperado = new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, request.fotoUrl(), TipoOperacaoSebo.VIRTUAL);

        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.save(seboCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(seboMapper.toResponse(seboCaptor.capture())).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.criar(contaSebo, request);

        assertThat(resultado).isEqualTo(responseEsperado);
        Sebo persistido = seboCaptor.getAllValues().get(0);
        assertThat(persistido.getAccount()).isSameAs(contaSebo);
        assertThat(persistido.getDescricao()).isEqualTo(request.descricao());
        assertThat(persistido.getTelefone()).isEqualTo(request.telefone());
        assertThat(persistido.getFotoUrl()).isEqualTo(request.fotoUrl());
        assertThat(persistido.getDataCriacao()).isNotNull();
        assertThat(persistido.getUltimaAtividade()).isNotNull();
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
                .build();
        SeboResponse responseEsperado = new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, request.fotoUrl(), TipoOperacaoSebo.VIRTUAL);

        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.of(existente));
        when(seboMapper.toResponse(existente)).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.atualizarMeuSebo(contaSebo, request);

        assertThat(resultado).isEqualTo(responseEsperado);
        assertThat(existente.getDescricao()).isEqualTo(request.descricao());
        assertThat(existente.getUltimaAtividade()).isNotNull();
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
        Sebo sebo = Sebo.builder().account(contaSebo).build();
        SeboResponse responseEsperado = new SeboResponse(7L, 10L, "Sebo do Joao", "sebo@vitral.com",
                null, null, "12345678000199", StatusVerificacaoSebo.VERIFICADO, null,
                null, null, null, null, null, TipoOperacaoSebo.VIRTUAL);

        when(seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(
                7L, StatusVerificacaoSebo.VERIFICADO)).thenReturn(Optional.of(sebo));
        when(seboMapper.toResponse(sebo)).thenReturn(responseEsperado);

        SeboResponse resultado = seboService.buscarPorId(7L);

        assertThat(resultado).isEqualTo(responseEsperado);
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar sebo por id inexistente")
    void shouldThrowNotFoundWhenSeboIdDoesNotExist() {
        when(seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(
                99L, StatusVerificacaoSebo.VERIFICADO)).thenReturn(Optional.empty());

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
    @DisplayName("Deve assumir sebo virtual quando o tipo de operacao nao for informado")
    void shouldDefaultToVirtualWhenTipoOperacaoIsNotProvided() {
        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.save(any(Sebo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seboMapper.toResponse(any(Sebo.class))).thenAnswer(invocation -> new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, request.fotoUrl(), TipoOperacaoSebo.VIRTUAL, false));

        SeboResponse response = seboService.criar(contaSebo, request);

        assertThat(response.tipoOperacao()).isEqualTo(TipoOperacaoSebo.VIRTUAL);
    }

    @Test
    @DisplayName("Deve criar sebo fisico quando endereco minimo for informado")
    void shouldCreatePhysicalSeboWhenAddressIsProvided() {
        SeboRequest requestFisico = new SeboRequest(
                "Sebo familiar com livros raros",
                "8399999-0000",
                "12345678000199",
                "https://cdn/foto.jpg",
                TipoOperacaoSebo.FISICO,
                "58.000-000",
                "Rua das Letras",
                "Joao Pessoa",
                "pb",
                "Seg a sex");

        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.existsByCnpj("12345678000199")).thenReturn(false);
        when(seboRepository.save(any(Sebo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seboMapper.toResponse(any(Sebo.class))).thenAnswer(invocation -> new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                requestFisico.descricao(), requestFisico.telefone(), requestFisico.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, requestFisico.fotoUrl(), TipoOperacaoSebo.FISICO,
                "58000000", "Rua das Letras", "Joao Pessoa", "PB", "Seg a sex", false));

        seboService.criar(contaSebo, requestFisico);

        verify(seboRepository).save(seboCaptor.capture());
        assertThat(seboCaptor.getValue().getTipoOperacao()).isEqualTo(TipoOperacaoSebo.FISICO);
        assertThat(seboCaptor.getValue().getCep()).isEqualTo("58000000");
        assertThat(seboCaptor.getValue().getUf()).isEqualTo("PB");
    }

    @Test
    @DisplayName("Deve criar sebo hibrido quando endereco minimo for informado")
    void shouldCreateHybridSeboWhenAddressIsProvided() {
        SeboRequest requestHibrido = new SeboRequest(
                "Sebo com atendimento online e retirada",
                "8399999-0000",
                "12345678000199",
                "https://cdn/foto.jpg",
                TipoOperacaoSebo.HIBRIDO,
                "58000-123",
                "Avenida Central",
                "Campina Grande",
                "pb",
                null);

        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.existsByCnpj("12345678000199")).thenReturn(false);
        when(seboRepository.save(any(Sebo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seboMapper.toResponse(any(Sebo.class))).thenAnswer(invocation -> new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                requestHibrido.descricao(), requestHibrido.telefone(), requestHibrido.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, requestHibrido.fotoUrl(), TipoOperacaoSebo.HIBRIDO,
                "58000123", "Avenida Central", "Campina Grande", "PB", null, false));

        SeboResponse response = seboService.criar(contaSebo, requestHibrido);

        verify(seboRepository).save(seboCaptor.capture());
        assertThat(response.tipoOperacao()).isEqualTo(TipoOperacaoSebo.HIBRIDO);
        assertThat(seboCaptor.getValue().getTipoOperacao()).isEqualTo(TipoOperacaoSebo.HIBRIDO);
        assertThat(seboCaptor.getValue().getCep()).isEqualTo("58000123");
        assertThat(seboCaptor.getValue().getUf()).isEqualTo("PB");
    }

    @Test
    @DisplayName("Deve rejeitar sebo fisico sem endereco minimo")
    void shouldRejectPhysicalSeboWithoutRequiredAddress() {
        SeboRequest requestFisico = new SeboRequest(
                "Sebo familiar com livros raros",
                "8399999-0000",
                "12345678000199",
                "https://cdn/foto.jpg",
                TipoOperacaoSebo.FISICO);

        when(seboRepository.existsByAccountId(10L)).thenReturn(false);
        when(seboRepository.existsByCnpj("12345678000199")).thenReturn(false);

        assertThatThrownBy(() -> seboService.criar(contaSebo, requestFisico))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CEP, logradouro, cidade e UF");
    }

    @Test
    @DisplayName("Deve limpar campos fisicos quando modalidade virtual for informada")
    void shouldClearPhysicalFieldsWhenVirtualModeIsProvided() {
        Sebo existente = Sebo.builder()
                .account(contaSebo)
                .tipoOperacao(TipoOperacaoSebo.HIBRIDO)
                .cep("58000000")
                .logradouro("Rua Antiga")
                .cidade("Joao Pessoa")
                .uf("PB")
                .horarioFuncionamento("Seg a sex")
                .build();

        when(seboRepository.findByAccountId(10L)).thenReturn(Optional.of(existente));
        when(seboMapper.toResponse(existente)).thenAnswer(invocation -> new SeboResponse(1L, 10L, "Sebo do Joao", "sebo@vitral.com",
                request.descricao(), request.telefone(), request.cnpj(), StatusVerificacaoSebo.PENDENTE,
                null, null, null, null, null, request.fotoUrl(), TipoOperacaoSebo.VIRTUAL, false));

        seboService.atualizarMeuSebo(contaSebo, request);

        assertThat(existente.getTipoOperacao()).isEqualTo(TipoOperacaoSebo.VIRTUAL);
        assertThat(existente.getCep()).isNull();
        assertThat(existente.getCidade()).isNull();
    }

    @Test
    @DisplayName("Deve anonimizar e desativar conta do sebo autenticado")
    void excluirConta_desativaAccount() {
        MensagemResponse response = seboService.excluirConta(contaSebo);

        verify(anonimizacaoContaService).anonimizarSebo(contaSebo);
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
