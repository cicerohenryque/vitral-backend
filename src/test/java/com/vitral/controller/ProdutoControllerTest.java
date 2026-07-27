package com.vitral.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.VendaFisicaRequest;
import com.vitral.dto.VendedorProdutoResponse;
import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.ProdutoService;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoController controller;

    @Test
    void criarAtualizarERemoverProdutoDoSebo() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        ProdutoRequest request = request();
        ProdutoResponse response = produto(30L, BigDecimal.valueOf(45), 4);
        when(produtoService.criar(account, request)).thenReturn(response);
        when(produtoService.atualizar(account, 30L, request)).thenReturn(response);

        assertThat(controller.criar(principal, request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(controller.atualizar(principal, 30L, request).getBody()).isSameAs(response);
        assertThat(controller.remover(principal, 30L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(produtoService).criar(account, request);
        verify(produtoService).atualizar(account, 30L, request);
        verify(produtoService).remover(account, 30L);
    }

    @Test
    void registrarVendaFisicaSincronizaEstoquePeloServico() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        VendaFisicaRequest request = new VendaFisicaRequest(2);
        ProdutoResponse response = produto(30L, BigDecimal.valueOf(45), 2);
        when(produtoService.registrarVendaFisica(account, 30L, request)).thenReturn(response);

        var result = controller.registrarVendaFisica(principal, 30L, request);

        assertThat(result.getBody()).isSameAs(response);
        verify(produtoService).registrarVendaFisica(account, 30L, request);
    }

    @Test
    void listarVendedoresRetornaSebosComProdutoAtivo() {
        List<VendedorProdutoResponse> vendedores = List.of(
                new VendedorProdutoResponse(30L, 10L, "Sebo A", BigDecimal.valueOf(45),
                        BigDecimal.valueOf(39), BigDecimal.valueOf(39), 3, CondicaoProduto.USADO),
                new VendedorProdutoResponse(31L, 11L, "Sebo B", BigDecimal.valueOf(48),
                        null, BigDecimal.valueOf(48), 2, CondicaoProduto.SEMINOVO));
        when(produtoService.listarVendedores(30L)).thenReturn(vendedores);

        var result = controller.listarVendedores(30L);

        assertThat(result.getBody()).containsExactlyElementsOf(vendedores);
        verify(produtoService).listarVendedores(30L);
    }

    @Test
    void consultasPublicasConvertemPaginacao() {
        var pageable = PageRequest.of(0, 10);
        ProdutoResponse produto = produto(30L, BigDecimal.valueOf(45), 3);
        when(produtoService.buscarPorId(30L)).thenReturn(produto);
        when(produtoService.listarPorSebo(10L, pageable)).thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(produtoService.listarPorCategoria(5L, null, pageable)).thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(produtoService.listarLancamentos(pageable)).thenReturn(new PageImpl<>(List.of(produto), pageable, 1));
        when(produtoService.listarClassicos(pageable)).thenReturn(new PageImpl<>(List.of(produto), pageable, 1));

        assertThat(controller.buscarPorId(30L).getBody()).isSameAs(produto);
        assertThat(controller.listarPorSebo(10L, pageable).getBody().content()).containsExactly(produto);
        assertThat(controller.listarPorCategoria(5L, pageable).getBody().content()).containsExactly(produto);
        assertThat(controller.listarLancamentos(pageable).getBody().content()).containsExactly(produto);
        assertThat(controller.listarClassicos(pageable).getBody().content()).containsExactly(produto);
    }

    private Account account() {
        return Account.builder().type(AccountType.SEBO).email("sebo@vitral.com").passwordHash("hash").ativo(true).build();
    }

    private ProdutoRequest request() {
        return new ProdutoRequest("978857164", "Dom Casmurro", 5L, "Machado de Assis", "Classico",
                BigDecimal.valueOf(45), 4, CondicaoProduto.USADO, null);
    }

    private ProdutoResponse produto(Long id, BigDecimal preco, int estoque) {
        return new ProdutoResponse(id, 10L, 5L, "Literatura", null, "978857164", "Dom Casmurro",
                "Machado de Assis", "Classico", preco, null, estoque, CondicaoProduto.USADO, null, true);
    }
}
