package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.VendedorProdutoResponse;
import com.vitral.entity.Account;
import com.vitral.entity.BaseEntity;
import com.vitral.entity.Categoria;
import com.vitral.entity.CatalogoLivro;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.enumerations.TipoMovimentacaoEstoque;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @Mock
    private SeboService seboService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private PrecoService precoService;

    @Mock
    private CatalogoLivroService catalogoLivroService;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @InjectMocks
    private ProdutoService produtoService;

    @Captor
    private ArgumentCaptor<Produto> produtoCaptor;

    private Account contaDona;
    private Account contaOutroSebo;
    private Sebo seboDono;
    private Sebo seboOutro;
    private ProdutoRequest request;

    @BeforeEach
    void setUp() {
        contaDona = Account.builder()
                .name("Sebo Dono")
                .email("dono@vitral.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .build();
        setId(contaDona, 100L);

        contaOutroSebo = Account.builder()
                .name("Outro Sebo")
                .email("outro@vitral.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .build();
        setId(contaOutroSebo, 200L);

        seboDono = Sebo.builder().account(contaDona)
                .statusVerificacao(StatusVerificacaoSebo.VERIFICADO).build();
        setId(seboDono, 1L);

        seboOutro = Sebo.builder().account(contaOutroSebo)
                .statusVerificacao(StatusVerificacaoSebo.VERIFICADO).build();
        setId(seboOutro, 2L);

        request = new ProdutoRequest(
                null,
                "Dom Casmurro",
                null,
                "Machado de Assis",
                "Edicao de 1899, capa dura",
                new BigDecimal("49.90"),
                3,
                CondicaoProduto.USADO,
                "https://cdn/dom.jpg");
    }

    @Test
    @DisplayName("Deve criar produto vinculado ao sebo da conta autenticada com ativo igual a true")
    void shouldCreateProductLinkedToOwnerSeboWithAtivoTrue() {
        ProdutoResponse responseEsperado = new ProdutoResponse(1L, 1L, null, null, null, null, request.titulo(), request.autor(),
                request.descricao(), request.preco(), null, request.estoque(), request.condicao(), request.fotoUrl(), Boolean.TRUE);

        when(seboService.buscarEntidadePorAccount(100L)).thenReturn(seboDono);
        when(produtoRepository.save(produtoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(precoService.precoPromocionalVigente(any())).thenReturn(Optional.empty());
        when(produtoMapper.toResponse(produtoCaptor.capture(), any())).thenReturn(responseEsperado);

        ProdutoResponse resultado = produtoService.criar(contaDona, request);

        assertThat(resultado).isEqualTo(responseEsperado);
        Produto persistido = produtoCaptor.getAllValues().get(0);
        assertThat(persistido.getSebo()).isSameAs(seboDono);
        assertThat(persistido.getTitulo()).isEqualTo("Dom Casmurro");
        assertThat(persistido.getPreco()).isEqualByComparingTo("49.90");
        assertThat(persistido.getEstoque()).isEqualTo(3);
        assertThat(persistido.getCondicao()).isEqualTo(CondicaoProduto.USADO);
        assertThat(persistido.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve criar produto com estoque padrao e categoria quando estoque vier nulo")
    void criar_estoqueNulo_usaPadraoECategoria() {
        Categoria categoria = Categoria.builder().nome("Romance").slug("romance").build();
        setId(categoria, 7L);
        ProdutoRequest requestSemEstoque = new ProdutoRequest(null, "Dom Casmurro", 7L, "Machado",
                "Descricao", BigDecimal.TEN, null, CondicaoProduto.USADO, null);
        ProdutoResponse responseEsperado = new ProdutoResponse(1L, 1L, 7L, "Romance", null, null,
                "Dom Casmurro", "Machado", "Descricao", BigDecimal.TEN, null, 1, CondicaoProduto.USADO, null, true);

        when(seboService.buscarEntidadePorAccount(100L)).thenReturn(seboDono);
        when(categoriaService.buscarEntidade(7L)).thenReturn(categoria);
        when(produtoRepository.save(produtoCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(precoService.precoPromocionalVigente(any())).thenReturn(Optional.empty());
        when(produtoMapper.toResponse(produtoCaptor.capture(), any())).thenReturn(responseEsperado);

        ProdutoResponse resultado = produtoService.criar(contaDona, requestSemEstoque);

        assertThat(resultado).isSameAs(responseEsperado);
        assertThat(produtoCaptor.getAllValues().get(0).getEstoque()).isEqualTo(1);
        verify(movimentacaoEstoqueService).registrarAlteracao(any(), eq(contaDona), eq(TipoMovimentacaoEstoque.ENTRADA),
                eq(1), eq(0), eq(1), eq(BigDecimal.TEN), eq("Estoque inicial"));
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException quando conta autenticada nao possui sebo")
    void shouldPropagateNotFoundWhenAccountHasNoSebo() {
        when(seboService.buscarEntidadePorAccount(100L))
                .thenThrow(new ResourceNotFoundException("Sebo nao encontrado para esta conta"));

        assertThatThrownBy(() -> produtoService.criar(contaDona, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(produtoRepository, never()).save(produtoCaptor.capture());
    }

    @Test
    @DisplayName("Deve atualizar campos do produto quando o sebo dono e o da conta autenticada")
    void shouldUpdateProductWhenOwnedByAuthenticatedAccount() {
        Produto produto = Produto.builder()
                .sebo(seboDono)
                .titulo("Antigo")
                .preco(new BigDecimal("10.00"))
                .condicao(CondicaoProduto.NOVO)
                .ativo(Boolean.TRUE)
                .build();
        setId(produto, 50L);
        ProdutoResponse esperado = new ProdutoResponse(50L, 1L, null, null, null, null, request.titulo(), request.autor(),
                request.descricao(), request.preco(), null, request.estoque(), request.condicao(), request.fotoUrl(), Boolean.TRUE);

        when(produtoRepository.findById(50L)).thenReturn(Optional.of(produto));
        when(precoService.precoPromocionalVigente(any())).thenReturn(Optional.empty());
        when(produtoMapper.toResponse(eq(produto), any())).thenReturn(esperado);

        ProdutoResponse resultado = produtoService.atualizar(contaDona, 50L, request);

        assertThat(resultado).isEqualTo(esperado);
        assertThat(produto.getTitulo()).isEqualTo("Dom Casmurro");
        assertThat(produto.getPreco()).isEqualByComparingTo("49.90");
        assertThat(produto.getEstoque()).isEqualTo(3);
        assertThat(produto.getCondicao()).isEqualTo(CondicaoProduto.USADO);
        assertThat(produto.getFotoUrl()).isEqualTo("https://cdn/dom.jpg");
    }

    @Test
    @DisplayName("Deve lancar BusinessException com status 403 ao atualizar produto de outro sebo")
    void shouldRejectUpdateWhenProductBelongsToAnotherSebo() {
        Produto produtoDeOutroSebo = Produto.builder().sebo(seboOutro).ativo(Boolean.TRUE).build();
        setId(produtoDeOutroSebo, 77L);

        when(produtoRepository.findById(77L)).thenReturn(Optional.of(produtoDeOutroSebo));

        assertThatThrownBy(() -> produtoService.atualizar(contaDona, 77L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        assertThat(produtoDeOutroSebo.getTitulo()).isNull();
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao atualizar produto inexistente")
    void shouldThrowNotFoundWhenUpdatingMissingProduct() {
        when(produtoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(contaDona, 999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");
    }

    @Test
    @DisplayName("Deve marcar produto como inativo no soft delete preservando a entidade")
    void shouldSoftDeleteProductSettingAtivoFalse() {
        Produto produto = Produto.builder().sebo(seboDono).ativo(Boolean.TRUE).build();
        setId(produto, 30L);
        when(produtoRepository.findById(30L)).thenReturn(Optional.of(produto));

        produtoService.remover(contaDona, 30L);

        assertThat(produto.getAtivo()).isFalse();
        verify(produtoRepository, never()).delete(produto);
    }

    @Test
    @DisplayName("Deve lancar BusinessException com status 403 ao remover produto de outro sebo")
    void shouldRejectRemoveWhenProductBelongsToAnotherSebo() {
        Produto produtoDeOutroSebo = Produto.builder().sebo(seboOutro).ativo(Boolean.TRUE).build();
        setId(produtoDeOutroSebo, 80L);
        when(produtoRepository.findById(80L)).thenReturn(Optional.of(produtoDeOutroSebo));

        assertThatThrownBy(() -> produtoService.remover(contaDona, 80L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        assertThat(produtoDeOutroSebo.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar produto ativo na consulta publica por id")
    void shouldReturnActiveProductWhenSearchedById() {
        Produto produto = Produto.builder().sebo(seboDono).ativo(Boolean.TRUE).build();
        setId(produto, 5L);
        ProdutoResponse esperado = new ProdutoResponse(5L, 1L, null, null, null, null, "T", null, null,
                BigDecimal.TEN, null, 1, CondicaoProduto.NOVO, null, Boolean.TRUE);

        when(produtoRepository.findByIdAndAtivoTrue(5L)).thenReturn(Optional.of(produto));
        when(precoService.precoPromocionalVigente(any())).thenReturn(Optional.empty());
        when(produtoMapper.toResponse(eq(produto), any())).thenReturn(esperado);

        ProdutoResponse resultado = produtoService.buscarPorId(5L);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Deve esconder produto quando o sebo nao estiver verificado")
    void buscarPorId_seboNaoVerificado_lancaNotFound() {
        Sebo seboPendente = Sebo.builder().account(contaDona)
                .statusVerificacao(StatusVerificacaoSebo.PENDENTE).build();
        Produto produto = Produto.builder().sebo(seboPendente).ativo(Boolean.TRUE).build();
        setId(produto, 5L);
        when(produtoRepository.findByIdAndAtivoTrue(5L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> produtoService.buscarPorId(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");
    }

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao buscar produto inativo ou inexistente")
    void shouldThrowNotFoundWhenSearchingInactiveOrMissingProduct() {
        when(produtoRepository.findByIdAndAtivoTrue(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Produto nao encontrado");
    }

    @Test
    @DisplayName("Deve listar vendedores verificados do mesmo produto pelo menor preco efetivo")
    void shouldListVerifiedSellersByEffectivePrice() {
        CatalogoLivro catalogo = CatalogoLivro.builder().isbn("9788535914849").titulo("Dom Casmurro").build();
        setId(catalogo, 20L);
        Produto referencia = Produto.builder().sebo(seboDono).titulo("Dom Casmurro")
                .catalogoLivro(catalogo).preco(new BigDecimal("50.00")).estoque(2)
                .condicao(CondicaoProduto.USADO).ativo(true).build();
        setId(referencia, 10L);
        Produto concorrente = Produto.builder().sebo(seboOutro).titulo("dom casmurro").catalogoLivro(catalogo)
                .preco(new BigDecimal("40.00")).estoque(1).condicao(CondicaoProduto.SEMINOVO).ativo(true).build();
        setId(concorrente, 11L);

        when(produtoRepository.findByIdAndAtivoTrue(10L)).thenReturn(Optional.of(referencia));
        when(produtoRepository.vendedoresAtivosPorCatalogo(20L))
                .thenReturn(List.of(referencia, concorrente));
        when(precoService.precoPromocionalVigente(referencia)).thenReturn(Optional.of(new BigDecimal("45.00")));
        when(precoService.precoPromocionalVigente(concorrente)).thenReturn(Optional.empty());

        List<VendedorProdutoResponse> vendedores = produtoService.listarVendedores(10L);

        assertThat(vendedores).extracting(VendedorProdutoResponse::produtoId).containsExactly(11L, 10L);
        assertThat(vendedores.getFirst().precoEfetivo()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("Deve listar apenas o produto de referencia quando nao ha catalogo")
    void listarVendedores_semCatalogo_retornaReferencia() {
        Produto referencia = Produto.builder().sebo(seboDono).titulo("Avulso")
                .preco(new BigDecimal("30.00")).estoque(2).condicao(CondicaoProduto.USADO)
                .ativo(true).build();
        setId(referencia, 10L);
        when(produtoRepository.findByIdAndAtivoTrue(10L)).thenReturn(Optional.of(referencia));
        when(precoService.precoPromocionalVigente(referencia)).thenReturn(Optional.empty());

        List<VendedorProdutoResponse> vendedores = produtoService.listarVendedores(10L);

        assertThat(vendedores).hasSize(1);
        assertThat(vendedores.getFirst().produtoId()).isEqualTo(10L);
        assertThat(vendedores.getFirst().precoEfetivo()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("Deve registrar venda fisica pelo endpoint legado")
    void registrarVendaFisica_delegaParaMovimentacaoEstoque() {
        Produto produto = Produto.builder().sebo(seboDono).preco(new BigDecimal("50.00")).ativo(true).build();
        setId(produto, 10L);
        ProdutoResponse esperado = new ProdutoResponse(10L, 1L, null, null, null, null, "Dom", null, null,
                BigDecimal.TEN, null, 2, CondicaoProduto.USADO, null, true);
        when(produtoRepository.findById(10L)).thenReturn(Optional.of(produto));
        when(precoService.precoEfetivo(produto)).thenReturn(new BigDecimal("45.00"));
        when(precoService.precoPromocionalVigente(produto)).thenReturn(Optional.of(new BigDecimal("45.00")));
        when(produtoMapper.toResponse(produto, new BigDecimal("45.00"))).thenReturn(esperado);

        ProdutoResponse response = produtoService.registrarVendaFisica(contaDona, 10L, new com.vitral.dto.VendaFisicaRequest(2));

        assertThat(response).isSameAs(esperado);
        verify(movimentacaoEstoqueService).registrarVendaFisica(eq(contaDona),
                org.mockito.ArgumentMatchers.argThat(req -> req.produtoId().equals(10L)
                        && req.quantidade().equals(2)
                        && req.valorUnitario().compareTo(new BigDecimal("45.00")) == 0));
    }

    @Test
    @DisplayName("Deve listar produtos ativos de um sebo de forma paginada")
    void shouldListActiveProductsBySeboPaged() {
        Produto p1 = Produto.builder().sebo(seboDono).ativo(Boolean.TRUE).build();
        setId(p1, 1L);
        Produto p2 = Produto.builder().sebo(seboDono).ativo(Boolean.TRUE).build();
        setId(p2, 2L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produto> pagina = new PageImpl<>(List.of(p1, p2), pageable, 2);
        ProdutoResponse r1 = new ProdutoResponse(1L, 1L, null, null, null, null, "A", null, null, BigDecimal.ONE, null, 1, CondicaoProduto.NOVO, null, true);
        ProdutoResponse r2 = new ProdutoResponse(2L, 1L, null, null, null, null, "B", null, null, BigDecimal.ONE, null, 1, CondicaoProduto.NOVO, null, true);

        when(produtoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(pagina);
        when(precoService.precosPromocionaisVigentes(any())).thenReturn(Map.of());
        when(produtoMapper.toResponse(eq(p1), any())).thenReturn(r1);
        when(produtoMapper.toResponse(eq(p2), any())).thenReturn(r2);

        Page<ProdutoResponse> resultado = produtoService.listarPorSebo(1L, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("Deve listar produtos ativos por categoria")
    void listarPorCategoria_retornaProdutosAtivos() {
        Categoria categoria = Categoria.builder().nome("Romance").slug("romance").build();
        setId(categoria, 7L);
        Produto produto = Produto.builder().sebo(seboDono).categoria(categoria).ativo(true).build();
        setId(produto, 1L);
        Pageable pageable = PageRequest.of(0, 10);
        ProdutoResponse response = new ProdutoResponse(1L, 1L, 7L, "Romance", null, null, "A", null, null,
                BigDecimal.ONE, null, 1, CondicaoProduto.NOVO, null, true);
        when(categoriaService.buscarEntidade(7L)).thenReturn(categoria);
        when(produtoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(precoService.precosPromocionaisVigentes(any())).thenReturn(Map.of());
        when(produtoMapper.toResponse(eq(produto), any())).thenReturn(response);

        Page<ProdutoResponse> resultado = produtoService.listarPorCategoria(7L, pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(categoriaService).buscarEntidade(7L);
    }

    @Test
    @DisplayName("Deve listar lancamentos usando produtos ativos na ordenacao recebida")
    void listarLancamentos_retornaProdutosAtivos() {
        Produto produto = Produto.builder().sebo(seboDono).ativo(true).build();
        setId(produto, 1L);
        Pageable pageable = PageRequest.of(0, 10);
        ProdutoResponse response = new ProdutoResponse(1L, 1L, null, null, null, null, "Lancamento", null, null,
                BigDecimal.TEN, null, 2, CondicaoProduto.NOVO, null, true);
        when(produtoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(precoService.precosPromocionaisVigentes(any())).thenReturn(Map.of());
        when(produtoMapper.toResponse(eq(produto), any())).thenReturn(response);

        Page<ProdutoResponse> resultado = produtoService.listarLancamentos(pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(produtoRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve listar classicos usando produtos ativos ordenados por data de criacao ascendente")
    void listarClassicos_retornaProdutosMaisAntigos() {
        Produto produto = Produto.builder().sebo(seboDono).ativo(true).build();
        setId(produto, 1L);
        Pageable pageable = PageRequest.of(0, 10);
        ProdutoResponse response = new ProdutoResponse(1L, 1L, null, null, null, null, "Classico", null, null,
                BigDecimal.TEN, null, 1, CondicaoProduto.USADO, null, true);
        when(produtoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(precoService.precosPromocionaisVigentes(any())).thenReturn(Map.of());
        when(produtoMapper.toResponse(eq(produto), any())).thenReturn(response);

        Page<ProdutoResponse> resultado = produtoService.listarClassicos(pageable);

        assertThat(resultado.getContent()).containsExactly(response);
        verify(produtoRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve retornar pagina vazia quando nao existem produtos classicos ativos")
    void listarClassicos_semProdutos_retornaPaginaVazia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(produtoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ProdutoResponse> resultado = produtoService.listarClassicos(pageable);

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
        verify(produtoRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
