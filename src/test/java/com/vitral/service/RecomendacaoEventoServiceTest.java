package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vitral.config.RecomendacaoProperties;
import com.vitral.entity.Account;
import com.vitral.entity.BaseEntity;
import com.vitral.entity.Categoria;
import com.vitral.entity.Pedido;
import com.vitral.entity.Produto;
import com.vitral.entity.RecomendacaoEvento;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.BookGenre;
import com.vitral.enumerations.TipoEventoRecomendacao;
import com.vitral.repository.RecomendacaoEventoRepository;

@ExtendWith(MockitoExtension.class)
class RecomendacaoEventoServiceTest {
    @Mock RecomendacaoEventoRepository repository;
    @Mock RecomendacaoEventoPersistenceService persistence;
    private RecomendacaoEventoService service;
    private Account usuario;
    private Produto produto;

    @BeforeEach void setUp() {
        service = new RecomendacaoEventoService(repository, persistence, new NormalizadorPesquisaService(),
                new RecomendacaoProperties(1000, 200, 20, 30, 365, 30, 3, 90, 50));
        usuario = Account.builder().type(AccountType.USUARIO).build(); setId(usuario, 1L);
        Categoria categoria = Categoria.builder().nome("Livros").slug("livros").build(); setId(categoria, 2L);
        Sebo sebo = Sebo.builder().build(); setId(sebo, 3L);
        produto = Produto.builder().categoria(categoria).sebo(sebo).bookGenre(BookGenre.FICCAO)
                .autor("Clarice Lispector").preco(BigDecimal.valueOf(42)).build(); setId(produto, 4L);
    }

    @Test void registraSnapshotEstruturadoDeProduto() {
        service.registrarProduto(usuario, TipoEventoRecomendacao.FAVORITO_ADICIONADO, produto);
        ArgumentCaptor<RecomendacaoEvento> captor = ArgumentCaptor.forClass(RecomendacaoEvento.class);
        verify(persistence).salvar(captor.capture());
        assertThat(captor.getValue()).satisfies(e -> {
            assertThat(e.getTipoProduto()).isEqualTo("livros");
            assertThat(e.getGenero()).isEqualTo("FICCAO");
            assertThat(e.getAutorArtista()).isEqualTo("clarice lispector");
            assertThat(e.getFaixaPreco()).isEqualTo("DE_25_A_50");
        });
    }

    @Test void naoRegistraVisitanteNemSebo() {
        service.registrarProduto(null, TipoEventoRecomendacao.VISUALIZACAO, produto);
        Account sebo = Account.builder().type(AccountType.SEBO).build(); setId(sebo, 9L);
        service.registrarProduto(sebo, TipoEventoRecomendacao.VISUALIZACAO, produto);
        verify(persistence, never()).salvar(any());
    }

    @Test void limitaVisualizacaoDuplicadaNaJanela() {
        when(repository.existsByAccountIdAndProdutoIdAndTipoAndCreatedAtAfter(any(), any(), any(), any()))
                .thenReturn(true);
        service.registrarProduto(usuario, TipoEventoRecomendacao.VISUALIZACAO, produto);
        verify(persistence, never()).salvar(any());
    }

    @Test void limitaContribuicaoDiariaDoMesmoProduto() {
        when(repository.countByAccountIdAndProdutoIdAndTipoAndCreatedAtAfter(any(), any(), any(), any()))
                .thenReturn(3L);
        service.registrarProduto(usuario, TipoEventoRecomendacao.VISUALIZACAO, produto);
        verify(persistence, never()).salvar(any());
    }

    @Test void normalizaPesquisaERegistraFiltros() {
        service.registrarPesquisa(usuario, "  Ficção Científica! ", produto.getCategoria(), BookGenre.FICCAO,
                BigDecimal.valueOf(20), BigDecimal.valueOf(60));
        ArgumentCaptor<RecomendacaoEvento> captor = ArgumentCaptor.forClass(RecomendacaoEvento.class);
        verify(persistence).salvar(captor.capture());
        assertThat(captor.getValue().getTermoPesquisa()).isEqualTo("ficcao cientifica");
        assertThat(captor.getValue().getFaixaPreco()).isEqualTo("DE_25_A_50");
    }

    @Test void naoRegistraPesquisaSemTermoNemFiltro() {
        service.registrarPesquisa(usuario, " ", null, null, null, null);
        verify(persistence, never()).salvar(any());
    }

    @Test void naoRegistraPesquisaIgualEmJanelaCurta() {
        when(repository.existsByAccountIdAndTipoAndTermoPesquisaAndCreatedAtAfter(any(), any(), any(), any()))
                .thenReturn(true);
        service.registrarPesquisa(usuario, "ficcao", null, null, null, null);
        verify(persistence, never()).salvar(any());
    }

    @Test void compraEIdempotentePorPedidoEProduto() {
        Pedido pedido = Pedido.builder().build(); setId(pedido, 8L);
        when(repository.existsByPedidoIdAndProdutoIdAndTipo(8L, 4L, TipoEventoRecomendacao.COMPRA_CONCLUIDA))
                .thenReturn(true);
        service.registrarCompra(usuario, pedido, produto);
        verify(persistence, never()).salvar(any());
    }

    @Test void falhaDoRastreamentoNaoInterrompeFluxoPrincipal() {
        when(repository.existsByAccountIdAndProdutoIdAndTipoAndCreatedAtAfter(any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("banco indisponivel"));
        assertThatCode(() -> service.registrarProduto(usuario, TipoEventoRecomendacao.VISUALIZACAO, produto))
                .doesNotThrowAnyException();
    }

    private void setId(Object alvo, Long id) {
        try { Field f = BaseEntity.class.getDeclaredField("id"); f.setAccessible(true); f.set(alvo, id); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
