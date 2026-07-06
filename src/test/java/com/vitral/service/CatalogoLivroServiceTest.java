package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vitral.dto.ProdutoRequest;
import com.vitral.entity.CatalogoLivro;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.exception.BusinessException;
import com.vitral.repository.CatalogoLivroRepository;

@ExtendWith(MockitoExtension.class)
class CatalogoLivroServiceTest {

    @Mock
    private CatalogoLivroRepository repository;

    @InjectMocks
    private CatalogoLivroService service;

    @Test
    void retornaNullQuandoIsbnNaoInformado() {
        assertThat(service.obterOuCriar(request(null))).isNull();
        assertThat(service.obterOuCriar(request("   "))).isNull();
        verifyNoInteractions(repository);
    }

    @Test
    void reutilizaLivroExistenteComIsbnNormalizado() {
        CatalogoLivro existente = CatalogoLivro.builder().isbn("9788535914849").titulo("Dom Casmurro").build();
        when(repository.findByIsbn("9788535914849")).thenReturn(Optional.of(existente));

        var result = service.obterOuCriar(request("978-85-359-1484-9"));

        assertThat(result).isSameAs(existente);
        verify(repository).findByIsbn("9788535914849");
    }

    @Test
    void criaLivroQuandoIsbnValidoNaoExiste() {
        when(repository.findByIsbn("8535914846")).thenReturn(Optional.empty());
        when(repository.save(any(CatalogoLivro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.obterOuCriar(request("8535914846"));

        assertThat(result.getIsbn()).isEqualTo("8535914846");
        assertThat(result.getTitulo()).isEqualTo("Dom Casmurro");
        assertThat(result.getAutores()).isEqualTo("Machado de Assis");
        assertThat(result.getCapaUrl()).isEqualTo("/capa.png");
    }

    @Test
    void aceitaIsbn10ComDigitoX() {
        assertThat(service.normalizarEValidar("0-8044-2957-X")).isEqualTo("080442957X");
    }

    @Test
    void rejeitaIsbnComFormatoOuChecksumInvalidos() {
        assertThatThrownBy(() -> service.normalizarEValidar("abc"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISBN invalido");
        assertThatThrownBy(() -> service.normalizarEValidar("9788535914840"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISBN invalido");
    }

    private ProdutoRequest request(String isbn) {
        return new ProdutoRequest(isbn, "Dom Casmurro", 1L, "Machado de Assis", "Classico",
                BigDecimal.valueOf(45), 3, CondicaoProduto.USADO, "/capa.png");
    }
}
