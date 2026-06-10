package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.data.jpa.domain.Specification;

import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.SeboResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class BuscaServiceTest {

    @Mock
    private SeboRepository seboRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private SeboMapper seboMapper;

    @Mock
    private ProdutoMapper produtoMapper;

    @InjectMocks
    private BuscaService buscaService;

    private Pageable pageable;
    private Sebo sebo;
    private SeboResponse seboResponse;
    private Produto produto;
    private ProdutoResponse produtoResponse;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        Account conta = Account.builder()
                .name("Livraria Leitura")
                .email("livraria@email.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .build();

        sebo = Sebo.builder()
                .account(conta)
                .cidade("Campina Grande")
                .uf("PB")
                .build();

        seboResponse = new SeboResponse(1L, 1L, "Livraria Leitura", "livraria@email.com",
                null, null, null, null, "Campina Grande", "PB", null, null);

        produto = Produto.builder()
                .sebo(sebo)
                .titulo("Dom Casmurro")
                .preco(BigDecimal.valueOf(25.00))
                .condicao(CondicaoProduto.USADO)
                .ativo(true)
                .build();

        produtoResponse = new ProdutoResponse(1L, 1L, "Dom Casmurro", null, null,
                BigDecimal.valueOf(25.00), CondicaoProduto.USADO, null, true);
    }

    @Test
    @DisplayName("Deve retornar sebos filtrados quando parametros sao fornecidos")
    void shouldReturnFilteredSebosWhenParametersProvided() {
        Page<Sebo> pageResult = new PageImpl<>(List.of(sebo));
        when(seboRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(seboMapper.toResponse(sebo)).thenReturn(seboResponse);

        Page<SeboResponse> resultado = buscaService.buscarSebos("Livraria", "Campina Grande", null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst()).isEqualTo(seboResponse);
        verify(seboRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve listar todos os sebos quando nenhum filtro for informado")
    void shouldListAllSebosWhenNoFiltersProvided() {
        Page<Sebo> pageResult = new PageImpl<>(List.of(sebo));
        when(seboRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(seboMapper.toResponse(sebo)).thenReturn(seboResponse);

        Page<SeboResponse> resultado = buscaService.buscarSebos(null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(seboRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve filtrar sebos por estado quando apenas uf for informada")
    void shouldFilterSebosByUfWhenOnlyUfProvided() {
        Page<Sebo> pageResult = new PageImpl<>(List.of(sebo));
        when(seboRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(seboMapper.toResponse(sebo)).thenReturn(seboResponse);

        Page<SeboResponse> resultado = buscaService.buscarSebos(null, null, "PB", pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(seboRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve buscar produtos ativos por titulo quando o termo for informado")
    void shouldSearchActiveProductsByTitleWhenTermoProvided() {
        Page<Produto> pageResult = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        Page<ProdutoResponse> resultado = buscaService.buscarProdutos("Dom", null, null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().getFirst().titulo()).isEqualTo("Dom Casmurro");
        verify(produtoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve filtrar produtos por sebo quando seboId for informado")
    void shouldFilterProductsBySeboWhenSeboIdProvided() {
        Page<Produto> pageResult = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        Page<ProdutoResponse> resultado = buscaService.buscarProdutos(null, 1L, null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(produtoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve filtrar produtos por condicao quando condicao for informada")
    void shouldFilterProductsByCondicaoWhenCondicaoProvided() {
        Page<Produto> pageResult = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        Page<ProdutoResponse> resultado = buscaService.buscarProdutos(null, null, CondicaoProduto.USADO, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(produtoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve filtrar produtos por faixa de preco quando precoMin e precoMax forem informados")
    void shouldFilterProductsByPriceRangeWhenBothLimitsProvided() {
        Page<Produto> pageResult = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        Page<ProdutoResponse> resultado = buscaService.buscarProdutos(
                null, null, null, BigDecimal.valueOf(10), BigDecimal.valueOf(50), pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(produtoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve listar todos os produtos ativos quando nenhum filtro for informado")
    void shouldListAllActiveProductsWhenNoFiltersProvided() {
        Page<Produto> pageResult = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageResult);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        Page<ProdutoResponse> resultado = buscaService.buscarProdutos(null, null, null, null, null, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        verify(produtoRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Deve retornar pagina vazia quando nenhum resultado for encontrado")
    void shouldReturnEmptyPageWhenNoResultsFound() {
        when(seboRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        Page<SeboResponse> resultado = buscaService.buscarSebos("Inexistente", null, null, pageable);

        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }
}
