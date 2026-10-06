package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import com.vitral.dto.AdminAccountResponse;
import com.vitral.dto.AdminDashboardResponse;
import com.vitral.dto.AdminProdutoResponse;
import com.vitral.entity.Account;
import com.vitral.entity.AdminAudit;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusPedido;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.AdminAuditRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private SeboRepository seboRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private AdminAuditRepository adminAuditRepository;

    @InjectMocks
    private AdminService adminService;

    @Captor
    private ArgumentCaptor<AdminAudit> auditCaptor;

    @Test
    @DisplayName("Deve montar o dashboard com os totais das contas, produtos, sebos e pedidos")
    void dashboard_deveRetornarTotais() {
        when(accountRepository.countByType(AccountType.USUARIO)).thenReturn(10L);
        when(accountRepository.countByTypeAndAtivoTrue(AccountType.USUARIO)).thenReturn(7L);
        when(seboRepository.count()).thenReturn(3L);
        when(produtoRepository.count()).thenReturn(20L);
        when(produtoRepository.countByAtivoTrue()).thenReturn(15L);
        when(pedidoRepository.count()).thenReturn(8L);
        when(pedidoRepository.countByStatusGrouped()).thenReturn(List.of(
                new Object[]{StatusPedido.CONFIRMADO, 5L},
                new Object[]{StatusPedido.AGUARDANDO_CONFIRMACAO, 3L}
        ));

        AdminDashboardResponse response = adminService.dashboard();

        assertThat(response.usuarios()).isEqualTo(10L);
        assertThat(response.usuariosAtivos()).isEqualTo(7L);
        assertThat(response.sebos()).isEqualTo(3L);
        assertThat(response.produtos()).isEqualTo(20L);
        assertThat(response.produtosAtivos()).isEqualTo(15L);
        assertThat(response.pedidos()).isEqualTo(8L);
        assertThat(response.pedidosPorStatus())
                .containsEntry("CONFIRMADO", 5L)
                .containsEntry("AGUARDANDO_CONFIRMACAO", 3L);
    }

    @Test
    @DisplayName("Não deve permitir que o administrador altere o próprio status")
    void alterarStatusConta_naoDevePermitirAlterarPropriaConta() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account alvo = account(1L, AccountType.USUARIO, true);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> adminService.alterarStatusConta(admin, 1L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Você não pode desativar a própria conta administrativa.")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(adminAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve permitir alterar o status de outra conta administrativa")
    void alterarStatusConta_naoDevePermitirAlterarOutroAdmin() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account outroAdmin = account(2L, AccountType.ADMIN, true);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(outroAdmin));

        assertThatThrownBy(() -> adminService.alterarStatusConta(admin, 2L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Contas administrativas não podem ser alteradas por esta operação.");

        verify(adminAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar a conta sem persistir nem auditar quando o status já é o solicitado")
    void alterarStatusConta_statusJaAtualizadoNaoDeveAuditar() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account usuario = account(2L, AccountType.USUARIO, true);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(usuario));

        AdminAccountResponse response = adminService.alterarStatusConta(admin, 2L, true);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.ativo()).isTrue();
        verify(adminAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar conta, incrementar authVersion e registrar auditoria")
    void alterarStatusConta_deveDesativarEAuditar() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account usuario = account(2L, AccountType.USUARIO, true);
        usuario.setAuthVersion(3);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(usuario));

        AdminAccountResponse response = adminService.alterarStatusConta(admin, 2L, false);

        assertThat(response.ativo()).isFalse();
        assertThat(usuario.getAtivo()).isFalse();
        assertThat(usuario.getAuthVersion()).isEqualTo(4);

        verify(adminAuditRepository).save(auditCaptor.capture());
        AdminAudit audit = auditCaptor.getValue();
        assertThat(audit.getAdmin()).isSameAs(admin);
        assertThat(audit.getAcao()).isEqualTo("DESATIVOU");
        assertThat(audit.getEntidade()).isEqualTo("CONTA");
        assertThat(audit.getEntidadeId()).isEqualTo(2L);
        assertThat(audit.getDescricao()).isEqualTo("Conta desativada.");
    }

    @Test
    @DisplayName("Deve reativar conta e registrar a ação correspondente")
    void alterarStatusConta_deveReativarEAuditar() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account usuario = account(2L, AccountType.USUARIO, false);
        usuario.setAuthVersion(4);

        when(accountRepository.findById(2L)).thenReturn(Optional.of(usuario));

        adminService.alterarStatusConta(admin, 2L, true);

        assertThat(usuario.getAtivo()).isTrue();
        assertThat(usuario.getAuthVersion()).isEqualTo(5);
        verify(adminAuditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAcao()).isEqualTo("REATIVOU");
        assertThat(auditCaptor.getValue().getDescricao()).isEqualTo("Conta reativada.");
    }

    @Test
    @DisplayName("Deve impedir a ativação de produto sem estoque")
    void alterarStatusProduto_naoDeveAtivarSemEstoque() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Sebo sebo = Sebo.builder().account(account(10L, AccountType.SEBO, true)).build();
        Produto produto = Produto.builder()
                .sebo(sebo)
                .titulo("Livro")
                .preco(BigDecimal.TEN)
                .estoque(0)
                .ativo(false)
                .build();

        when(produtoRepository.findById(5L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> adminService.alterarStatusProduto(admin, 5L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível ativar um produto sem estoque.")
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(produto.getAtivo()).isFalse();
        verify(adminAuditRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve alterar status do produto e registrar auditoria")
    void alterarStatusProduto_deveAlterarEAuditar() {
        Account admin = account(1L, AccountType.ADMIN, true);
        Account seboAccount = account(10L, AccountType.SEBO, true);
        Sebo sebo = Sebo.builder().account(seboAccount).build();
        Produto produto = Produto.builder()
                .sebo(sebo)
                .titulo("Dom Casmurro")
                .preco(BigDecimal.valueOf(30))
                .estoque(2)
                .ativo(true)
                .build();

        when(produtoRepository.findById(5L)).thenReturn(Optional.of(produto));

        AdminProdutoResponse response = adminService.alterarStatusProduto(admin, 5L, false);

        assertThat(response.ativo()).isFalse();
        assertThat(produto.getAtivo()).isFalse();

        verify(adminAuditRepository).save(auditCaptor.capture());
        AdminAudit audit = auditCaptor.getValue();
        assertThat(audit.getAcao()).isEqualTo("DESATIVOU");
        assertThat(audit.getEntidade()).isEqualTo("PRODUTO");
        assertThat(audit.getEntidadeId()).isEqualTo(5L);
        assertThat(audit.getDescricao()).isEqualTo("Produto desativado: Dom Casmurro");
    }

    @Test
    @DisplayName("Deve retornar erro quando a conta administrativa consultada não existe")
    void buscarConta_deveLancarErroQuandoNaoEncontrada() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.buscarConta(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("A conta informada não foi encontrada.");
    }

    @Test
    @DisplayName("Deve retornar erro quando o produto consultado não existe")
    void buscarProduto_deveLancarErroQuandoNaoEncontrado() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.buscarProduto(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("O produto informado não foi encontrado.");
    }

    @Test
    @DisplayName("Deve listar contas usando paginação e retornar os dados administrativos")
    void listarContas_deveMapearResultados() {
        Account usuario = account(2L, AccountType.USUARIO, true);
        PageRequest pageable = PageRequest.of(0, 10);
        when(accountRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(usuario), pageable, 1));

        var page = adminService.listarContas(null, AccountType.USUARIO, true, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        AdminAccountResponse response = page.getContent().getFirst();
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.type()).isEqualTo(AccountType.USUARIO);
        assertThat(response.ativo()).isTrue();
    }

    private Account account(Long id, AccountType type, boolean ativo) {
        Account account = Account.builder()
                .name(type == AccountType.ADMIN ? "Admin" : "Usuario")
                .username("user" + id)
                .email("user" + id + "@vitral.com")
                .passwordHash("hash")
                .type(type)
                .emailVerificado(true)
                .authVersion(0)
                .ativo(ativo)
                .build();
        setId(account, id);
        return account;
    }

    private void setId(Object entity, Long id) {
        try {
            var field = com.vitral.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
