package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.vitral.dto.EstornarVendaFisicaRequest;
import com.vitral.dto.MovimentacaoEstoqueResponse;
import com.vitral.dto.RegistrarVendaFisicaRequest;
import com.vitral.entity.Account;
import com.vitral.entity.MovimentacaoEstoque;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.TipoMovimentacaoEstoque;
import com.vitral.enumerations.TipoOperacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.repository.MovimentacaoEstoqueRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueServiceTest {

    @Mock MovimentacaoEstoqueRepository repository;
    @Mock ProdutoRepository produtoRepository;
    @Mock PedidoRepository pedidoRepository;
    @Mock SeboService seboService;
    @InjectMocks MovimentacaoEstoqueService service;

    private Account operador;
    private Produto produto;

    @BeforeEach
    void setup() {
        operador = Account.builder().name("Sebo").email("sebo@test.com").type(AccountType.SEBO).build();
        ReflectionTestUtils.setField(operador, "id", 1L);
        Sebo sebo = Sebo.builder().account(operador).tipoOperacao(TipoOperacaoSebo.FISICO).build();
        ReflectionTestUtils.setField(sebo, "id", 2L);
        produto = Produto.builder().sebo(sebo).estoque(5).ativo(true).preco(new BigDecimal("30.00")).build();
        ReflectionTestUtils.setField(produto, "id", 3L);
        lenient().when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registraVendaFisicaComValorESaldos() {
        when(produtoRepository.findById(3L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.sumQuantidadeReservadaEmPedidosPendentes(3L)).thenReturn(1L);

        MovimentacaoEstoqueResponse response = service.registrarVendaFisica(operador,
                new RegistrarVendaFisicaRequest(3L, 2, new BigDecimal("25.00"), "Balcao"));

        assertThat(produto.getEstoque()).isEqualTo(3);
        assertThat(response.tipo()).isEqualTo(TipoMovimentacaoEstoque.VENDA_FISICA);
        assertThat(response.valorTotal()).isEqualByComparingTo("50.00");
        assertThat(response.estoqueAntes()).isEqualTo(5);
        assertThat(response.estoqueDepois()).isEqualTo(3);
        verify(seboService).registrarAtividade(produto.getSebo());
    }

    @Test
    void bloqueiaVendaFisicaParaSeboVirtual() {
        produto.getSebo().setTipoOperacao(TipoOperacaoSebo.VIRTUAL);
        when(produtoRepository.findById(3L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.registrarVendaFisica(operador,
                new RegistrarVendaFisicaRequest(3L, 1, BigDecimal.TEN, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("virtual");
        assertThat(produto.getEstoque()).isEqualTo(5);
    }

    @Test
    void impedeVendaQueConsomeEstoqueReservado() {
        when(produtoRepository.findById(3L)).thenReturn(Optional.of(produto));
        when(pedidoRepository.sumQuantidadeReservadaEmPedidosPendentes(3L)).thenReturn(3L);

        assertThatThrownBy(() -> service.registrarVendaFisica(operador,
                new RegistrarVendaFisicaRequest(3L, 3, BigDecimal.TEN, null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("reservadas");
        assertThat(produto.getEstoque()).isEqualTo(5);
    }

    @Test
    void estornoRepoeEstoque() {
        MovimentacaoEstoque origem = MovimentacaoEstoque.builder().produto(produto).sebo(produto.getSebo())
                .operador(operador).tipo(TipoMovimentacaoEstoque.VENDA_FISICA).quantidade(2)
                .estoqueAntes(5).estoqueDepois(3).valorUnitario(new BigDecimal("25.00")).build();
        ReflectionTestUtils.setField(origem, "id", 9L);
        produto.setEstoque(3);
        when(repository.findById(9L)).thenReturn(Optional.of(origem));
        when(repository.existsByMovimentacaoOrigemIdAndTipo(9L, TipoMovimentacaoEstoque.ESTORNO))
                .thenReturn(false);

        MovimentacaoEstoqueResponse response = service.estornarVendaFisica(
                operador, 9L, new EstornarVendaFisicaRequest("Cliente devolveu"));

        assertThat(produto.getEstoque()).isEqualTo(5);
        assertThat(response.tipo()).isEqualTo(TipoMovimentacaoEstoque.ESTORNO);
        assertThat(response.movimentacaoOrigemId()).isEqualTo(9L);
        verify(seboService).registrarAtividade(produto.getSebo());
    }
}
