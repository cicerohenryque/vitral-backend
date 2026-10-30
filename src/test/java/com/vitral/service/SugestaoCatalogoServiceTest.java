package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.AprovarSugestaoCatalogoRequest;
import com.vitral.dto.SugestaoCatalogoRequest;
import com.vitral.entity.Account;
import com.vitral.entity.BaseEntity;
import com.vitral.entity.Categoria;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.entity.SugestaoCatalogo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.BookGenre;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.StatusSugestaoCatalogo;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;
import com.vitral.repository.SugestaoCatalogoRepository;

@ExtendWith(MockitoExtension.class)
class SugestaoCatalogoServiceTest {

    @Mock SugestaoCatalogoRepository repository;
    @Mock SeboRepository seboRepository;
    @Mock CategoriaService categoriaService;
    @Mock ProdutoService produtoService;
    @Mock ProdutoRepository produtoRepository;
    @InjectMocks SugestaoCatalogoService service;

    private Account colaborador;
    private Account responsavel;
    private Account outroResponsavel;
    private Sebo sebo;
    private Categoria livros;
    private SugestaoCatalogoRequest request;

    @BeforeEach
    void setup() {
        colaborador = account(1L, "Usuário", AccountType.USUARIO);
        responsavel = account(2L, "Sebo", AccountType.SEBO);
        outroResponsavel = account(3L, "Outro", AccountType.SEBO);
        sebo = Sebo.builder().account(responsavel).statusVerificacao(StatusVerificacaoSebo.VERIFICADO).build();
        setId(sebo, 10L);
        livros = Categoria.builder().nome("Livros").slug("livros").build();
        setId(livros, 20L);
        request = new SugestaoCatalogoRequest(10L, "Dom Casmurro", 20L, BookGenre.ROMANCE,
                "Machado", "Clássico", 1899, new BigDecimal("25.00"), 1, CondicaoProduto.USADO, null);
    }

    @Test
    void colaboradorCriaSugestaoPendenteSemPublicarProduto() {
        when(seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(10L, StatusVerificacaoSebo.VERIFICADO))
                .thenReturn(Optional.of(sebo));
        when(categoriaService.buscarPermitida(20L)).thenReturn(livros);
        when(repository.save(any())).thenAnswer(invocation -> {
            SugestaoCatalogo value = invocation.getArgument(0); setId(value, 30L); return value;
        });

        var response = service.criar(colaborador, request);

        assertThat(response.status()).isEqualTo(StatusSugestaoCatalogo.PENDENTE);
        assertThat(response.catalogoNome()).isEqualTo("Sebo");
        assertThat(response.titulo()).isEqualTo("Dom Casmurro");
        verify(produtoService, never()).criar(any(), any());
    }

    @Test
    void categoriaDiferenteDeLivroEhAceitaSemGeneroLiterario() {
        Categoria discos = Categoria.builder().nome("Discos").slug("vinis").build();
        setId(discos, 20L);
        SugestaoCatalogoRequest disco = new SugestaoCatalogoRequest(10L, "Clube da Esquina", 20L, null,
                "Milton Nascimento", null, 1972, new BigDecimal("80.00"), 1, CondicaoProduto.USADO, null);
        when(seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(10L, StatusVerificacaoSebo.VERIFICADO))
                .thenReturn(Optional.of(sebo));
        when(categoriaService.buscarPermitida(20L)).thenReturn(discos);
        when(repository.save(any())).thenAnswer(invocation -> {
            SugestaoCatalogo value = invocation.getArgument(0); setId(value, 31L); return value;
        });

        var response = service.criar(colaborador, disco);

        assertThat(response.titulo()).isEqualTo("Clube da Esquina");
        assertThat(response.categoriaNome()).isEqualTo("Discos");
    }

    @Test
    void responsavelAprovaEProdutoEhCriadoNoProprioCatalogo() {
        SugestaoCatalogo sugestao = sugestaoPendente();
        ProdutoResponse responseProduto = new ProdutoResponse(99L, 10L, 20L, "Livros", "Dom Casmurro",
                "Machado", "Clássico", new BigDecimal("25.00"), null, 1, CondicaoProduto.USADO, null, true);
        Produto produto = Produto.builder().sebo(sebo).categoria(livros).titulo("Dom Casmurro")
                .preco(new BigDecimal("25.00")).estoque(1).condicao(CondicaoProduto.USADO).build();
        setId(produto, 99L);
        when(repository.findByIdParaDecisao(30L)).thenReturn(Optional.of(sugestao));
        when(produtoService.criar(any(), any())).thenReturn(responseProduto);
        when(produtoRepository.getReferenceById(99L)).thenReturn(produto);

        var response = service.aprovar(responsavel, 30L);

        assertThat(response.status()).isEqualTo(StatusSugestaoCatalogo.APROVADA);
        assertThat(response.produtoCriadoId()).isEqualTo(99L);
        assertThat(sugestao.getDecididoPor()).isSameAs(responsavel);
        verify(produtoService).criar(org.mockito.ArgumentMatchers.eq(responsavel), any());
    }

    @Test
    void responsavelPodeCorrigirPrecoEEstoqueAntesDePublicar() {
        SugestaoCatalogo sugestao = sugestaoPendente();
        ProdutoResponse responseProduto = new ProdutoResponse(99L, 10L, 20L, "Livros", "Dom Casmurro",
                "Machado", "Clássico", new BigDecimal("30.00"), null, 4, CondicaoProduto.USADO, null, true);
        Produto produto = Produto.builder().sebo(sebo).categoria(livros).titulo("Dom Casmurro")
                .preco(new BigDecimal("30.00")).estoque(4).condicao(CondicaoProduto.USADO).build();
        setId(produto, 99L);
        when(repository.findByIdParaDecisao(30L)).thenReturn(Optional.of(sugestao));
        when(categoriaService.buscarPermitida(20L)).thenReturn(livros);
        when(produtoService.criar(any(), any())).thenReturn(responseProduto);
        when(produtoRepository.getReferenceById(99L)).thenReturn(produto);
        var revisao = new AprovarSugestaoCatalogoRequest("Dom Casmurro", 20L, BookGenre.ROMANCE,
                "Machado", "Edição conferida", 1899, new BigDecimal("30.00"), 4,
                CondicaoProduto.USADO, null);
        ArgumentCaptor<ProdutoRequest> produtoRequest = ArgumentCaptor.forClass(ProdutoRequest.class);

        service.aprovar(responsavel, 30L, revisao);

        verify(produtoService).criar(org.mockito.ArgumentMatchers.eq(responsavel), produtoRequest.capture());
        assertThat(produtoRequest.getValue().preco()).isEqualByComparingTo("30.00");
        assertThat(produtoRequest.getValue().estoque()).isEqualTo(4);
        assertThat(sugestao.getDescricao()).isEqualTo("Edição conferida");
    }

    @Test
    void outroSeboNaoPodeDecidirSugestao() {
        when(repository.findByIdParaDecisao(30L)).thenReturn(Optional.of(sugestaoPendente()));

        assertThatThrownBy(() -> service.rejeitar(outroResponsavel, 30L, "Não conheço o item"))
                .isInstanceOf(BusinessException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
        verify(produtoService, never()).criar(any(), any());
    }

    @Test
    void sugestaoJaDecididaNaoPodeSerProcessadaNovamente() {
        SugestaoCatalogo sugestao = sugestaoPendente();
        sugestao.setStatus(StatusSugestaoCatalogo.REJEITADA);
        when(repository.findByIdParaDecisao(30L)).thenReturn(Optional.of(sugestao));

        assertThatThrownBy(() -> service.aprovar(responsavel, 30L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já foi analisada");
    }

    private SugestaoCatalogo sugestaoPendente() {
        SugestaoCatalogo value = SugestaoCatalogo.builder().usuario(colaborador).sebo(sebo).categoria(livros)
                .titulo(request.titulo()).autor(request.autor()).descricao(request.descricao()).ano(request.ano())
                .preco(request.preco()).estoque(1).condicao(request.condicao()).bookGenre(request.bookGenre()).build();
        setId(value, 30L);
        return value;
    }

    private Account account(Long id, String name, AccountType type) {
        Account value = Account.builder().name(name).username(name.toLowerCase()).email(name + "@vitral.test")
                .passwordHash("hash").type(type).ativo(true).build();
        setId(value, id); return value;
    }

    private static void setId(BaseEntity entity, Long id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id"); field.setAccessible(true); field.set(entity, id);
        } catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
    }
}
