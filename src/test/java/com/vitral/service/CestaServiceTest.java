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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vitral.dto.CestaItemResponse;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.entity.CestaItem;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.CestaItemRepository;
import com.vitral.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class CestaServiceTest {

    @Mock
    private CestaItemRepository cestaItemRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private CestaService cestaService;

    private Account buildUsuario() {
        return Account.builder()
                .name("Joao")
                .email("joao@email.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .emailVerificado(true)
                .build();
    }

    private Produto buildProduto() {
        Account seboAccount = Account.builder()
                .name("Sebo Teste")
                .email("sebo@email.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .emailVerificado(true)
                .build();
        Sebo sebo = Sebo.builder().account(seboAccount).build();
        return Produto.builder()
                .sebo(sebo)
                .titulo("Dom Casmurro")
                .autor("Machado de Assis")
                .preco(new BigDecimal("29.90"))
                .condicao(CondicaoProduto.USADO)
                .ativo(true)
                .build();
    }

    @Test
    @DisplayName("Deve lançar ConflictException quando produto já está na cesta")
    void adicionarItem_produtoJaNaCesta_lancaConflictException() {
        Account account = buildUsuario();
        when(cestaItemRepository.existsByAccountIdAndProdutoId(account.getId(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> cestaService.adicionarItem(account, 1L))
                .isInstanceOf(ConflictException.class);

        verify(produtoRepository, never()).findByIdAndAtivoTrue(any());
        verify(cestaItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando produto não existe ou está inativo")
    void adicionarItem_produtoNaoEncontrado_lancaResourceNotFoundException() {
        Account account = buildUsuario();
        when(cestaItemRepository.existsByAccountIdAndProdutoId(account.getId(), 99L)).thenReturn(false);
        when(produtoRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cestaService.adicionarItem(account, 99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cestaItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve adicionar item à cesta e retornar mensagem de sucesso")
    void adicionarItem_sucesso_salvaCestaItemERetornaMensagem() {
        Account account = buildUsuario();
        Produto produto = buildProduto();
        when(cestaItemRepository.existsByAccountIdAndProdutoId(account.getId(), 1L)).thenReturn(false);
        when(produtoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(produto));

        MensagemResponse response = cestaService.adicionarItem(account, 1L);

        assertThat(response.mensagem()).isEqualTo("Produto adicionado a cesta");
        verify(cestaItemRepository).save(any(CestaItem.class));
    }

    @Test
    @DisplayName("Deve retornar lista de itens mapeados para a cesta do usuário")
    void listarCesta_comItens_retornaListaMapeada() {
        Account account = buildUsuario();
        Produto produto = buildProduto();
        CestaItem cestaItem = CestaItem.builder().account(account).produto(produto).build();
        when(cestaItemRepository.findByAccountIdComProdutoESebo(account.getId()))
                .thenReturn(List.of(cestaItem));

        List<CestaItemResponse> resultado = cestaService.listarCesta(account);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().titulo()).isEqualTo("Dom Casmurro");
        assertThat(resultado.getFirst().preco()).isEqualByComparingTo("29.90");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover item que não está na cesta")
    void removerItem_itemNaoEncontrado_lancaResourceNotFoundException() {
        Account account = buildUsuario();
        when(cestaItemRepository.findByAccountIdAndProdutoId(account.getId(), 5L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cestaService.removerItem(account, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(cestaItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve remover item da cesta e retornar mensagem de sucesso")
    void removerItem_sucesso_deletaItemERetornaMensagem() {
        Account account = buildUsuario();
        Produto produto = buildProduto();
        CestaItem cestaItem = CestaItem.builder().account(account).produto(produto).build();
        when(cestaItemRepository.findByAccountIdAndProdutoId(account.getId(), 1L))
                .thenReturn(Optional.of(cestaItem));

        MensagemResponse response = cestaService.removerItem(account, 1L);

        assertThat(response.mensagem()).isEqualTo("Produto removido da cesta");
        verify(cestaItemRepository).delete(cestaItem);
    }
}
