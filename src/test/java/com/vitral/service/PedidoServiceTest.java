package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.vitral.dto.PedidoResponse;
import com.vitral.entity.Account;
import com.vitral.entity.CestaItem;
import com.vitral.entity.Pedido;
import com.vitral.entity.PedidoItem;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.StatusPedido;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.CestaItemRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CestaItemRepository cestaItemRepository;

    @Mock
    private SeboRepository seboRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    private Account buildUsuario() {
        Account account = Account.builder()
                .name("Carlos")
                .email("carlos@email.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .emailVerificado(true)
                .build();
        setId(account, 10L);
        return account;
    }

    private Account buildSeboAccount(Long id) {
        Account account = Account.builder()
                .name("Sebo Central")
                .email("sebo" + id + "@email.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .emailVerificado(true)
                .build();
        setId(account, id);
        return account;
    }

    private Sebo buildSebo(Account seboAccount, Long id) {
        Sebo sebo = Sebo.builder().account(seboAccount).cidade("Recife").uf("PE").build();
        setId(sebo, id);
        return sebo;
    }

    private Produto buildProduto(Sebo sebo) {
        return Produto.builder()
                .sebo(sebo)
                .titulo("O Cortiço")
                .autor("Aluísio Azevedo")
                .preco(new BigDecimal("25.00"))
                .condicao(CondicaoProduto.USADO)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a cesta está vazia")
    void confirmarPedido_cestaVazia_lancaBusinessException() {
        Account account = buildUsuario();
        when(cestaItemRepository.findByAccountIdComProdutoESebo(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> pedidoService.confirmarPedido(account))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cesta esta vazia");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a cesta contém produto inativo")
    void confirmarPedido_possuiProdutoInativo_lancaBusinessException() {
        Account account = buildUsuario();
        Sebo sebo = buildSebo(buildSeboAccount(1L), 1L);
        Produto produtoInativo = Produto.builder()
                .sebo(sebo)
                .titulo("Livro Esgotado")
                .preco(new BigDecimal("10.00"))
                .condicao(CondicaoProduto.USADO)
                .ativo(false)
                .build();
        CestaItem item = CestaItem.builder().account(account).produto(produtoInativo).build();
        when(cestaItemRepository.findByAccountIdComProdutoESebo(10L)).thenReturn(List.of(item));

        assertThatThrownBy(() -> pedidoService.confirmarPedido(account))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao estao mais disponiveis");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando produtos da cesta são de sebos distintos")
    void confirmarPedido_produtosDeSebosDistintos_lancaBusinessException() {
        Account account = buildUsuario();
        Sebo sebo1 = buildSebo(buildSeboAccount(1L), 1L);
        Sebo sebo2 = buildSebo(buildSeboAccount(2L), 2L);
        CestaItem item1 = CestaItem.builder().account(account).produto(buildProduto(sebo1)).build();
        CestaItem item2 = CestaItem.builder().account(account).produto(buildProduto(sebo2)).build();
        when(cestaItemRepository.findByAccountIdComProdutoESebo(10L)).thenReturn(List.of(item1, item2));

        assertThatThrownBy(() -> pedidoService.confirmarPedido(account))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mesmo sebo");

        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar pedido, limpar cesta e retornar response ao confirmar com sucesso")
    void confirmarPedido_sucesso_criaPedidoLimpaERetornaResponse() {
        Account account = buildUsuario();
        Sebo sebo = buildSebo(buildSeboAccount(1L), 1L);
        Produto produto = buildProduto(sebo);
        CestaItem item = CestaItem.builder().account(account).produto(produto).build();
        when(cestaItemRepository.findByAccountIdComProdutoESebo(10L)).thenReturn(List.of(item));
        when(seboRepository.findById(1L)).thenReturn(Optional.of(sebo));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.confirmarPedido(account);

        assertThat(response.status()).isEqualTo(StatusPedido.AGUARDANDO_CONFIRMACAO);
        assertThat(response.total()).isEqualByComparingTo("25.00");
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().getFirst().tituloSnapshot()).isEqualTo("O Cortiço");
        verify(cestaItemRepository).deleteByAccountId(10L);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando pedido não pertence ao sebo da conta")
    void confirmarCompra_pedidoNaoPertenceAoSebo_lancaBusinessException() {
        Account seboAccount = buildSeboAccount(1L);
        Account outroSeboAccount = buildSeboAccount(2L);
        Sebo outraSebo = buildSebo(outroSeboAccount, 2L);
        Pedido pedido = Pedido.builder()
                .account(buildUsuario())
                .sebo(outraSebo)
                .status(StatusPedido.AGUARDANDO_CONFIRMACAO)
                .total(new BigDecimal("25.00"))
                .build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarCompra(seboAccount, 1L, StatusPedido.CONFIRMADO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar alterar pedido já confirmado")
    void confirmarCompra_pedidoJaConfirmado_lancaBusinessException() {
        Account seboAccount = buildSeboAccount(1L);
        Sebo sebo = buildSebo(seboAccount, 1L);
        Pedido pedido = Pedido.builder()
                .account(buildUsuario())
                .sebo(sebo)
                .status(StatusPedido.CONFIRMADO)
                .total(new BigDecimal("25.00"))
                .build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarCompra(seboAccount, 1L, StatusPedido.CANCELADO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO_CONFIRMACAO");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao informar status inválido")
    void confirmarCompra_statusInvalido_lancaBusinessException() {
        Account seboAccount = buildSeboAccount(1L);
        Sebo sebo = buildSebo(seboAccount, 1L);
        Pedido pedido = Pedido.builder()
                .account(buildUsuario())
                .sebo(sebo)
                .status(StatusPedido.AGUARDANDO_CONFIRMACAO)
                .total(new BigDecimal("25.00"))
                .build();
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.confirmarCompra(seboAccount, 1L, StatusPedido.AGUARDANDO_CONFIRMACAO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Status invalido");
    }

    @Test
    @DisplayName("Deve atualizar status e retornar response ao confirmar compra com sucesso")
    void confirmarCompra_sucesso_atualizaStatusERetornaResponse() {
        Account seboAccount = buildSeboAccount(1L);
        Sebo sebo = buildSebo(seboAccount, 1L);
        Produto produto = buildProduto(sebo);
        Pedido pedido = Pedido.builder()
                .account(buildUsuario())
                .sebo(sebo)
                .status(StatusPedido.AGUARDANDO_CONFIRMACAO)
                .total(new BigDecimal("25.00"))
                .build();
        pedido.getItens().add(PedidoItem.builder()
                .pedido(pedido)
                .produto(produto)
                .tituloSnapshot("O Cortiço")
                .precoSnapshot(new BigDecimal("25.00"))
                .build());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        PedidoResponse response = pedidoService.confirmarCompra(seboAccount, 1L, StatusPedido.CONFIRMADO);

        assertThat(response.status()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    @DisplayName("Deve retornar página de pedidos do usuário ordenados por data decrescente")
    void historicoPedidosUsuario_retornaPagedResponse() {
        Account account = buildUsuario();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> paginaVazia = new PageImpl<>(List.of());
        when(pedidoRepository.findByAccountIdOrderByCreatedAtDesc(eq(10L), eq(pageable)))
                .thenReturn(paginaVazia);

        Page<PedidoResponse> resultado = pedidoService.historicoPedidosUsuario(account, pageable);

        assertThat(resultado.getContent()).isEmpty();
        verify(pedidoRepository).findByAccountIdOrderByCreatedAtDesc(10L, pageable);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando sebo não encontrado no histórico de vendas")
    void historicoVendasSebo_seboNaoEncontrado_lancaResourceNotFoundException() {
        Account seboAccount = buildSeboAccount(1L);
        Pageable pageable = PageRequest.of(0, 10);
        when(seboRepository.findByAccountId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.historicoVendasSebo(seboAccount, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
