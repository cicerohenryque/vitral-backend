package com.vitral.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.vitral.entity.Account;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.repository.ProdutoRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoSpecificationTest {
    @Autowired ProdutoRepository produtoRepository;
    @Autowired EntityManager entityManager;

    @BeforeEach
    void cadastrarProdutoAcentuado() {
        Account account = Account.builder().name("Sebo teste").username("sebo-teste")
                .email("sebo@teste.local").passwordHash("hash").type(AccountType.SEBO)
                .emailVerificado(true).ativo(true).build();
        entityManager.persist(account);
        Sebo sebo = Sebo.builder().account(account).statusVerificacao(StatusVerificacaoSebo.VERIFICADO).build();
        entityManager.persist(sebo);
        entityManager.persist(Produto.builder().sebo(sebo).titulo("Ficção brasileira").autor("João Guimarães")
                .preco(BigDecimal.TEN).estoque(1).condicao(CondicaoProduto.NOVO).ativo(true).build());
        entityManager.flush();
    }

    @Test
    void encontraTituloAcentuadoComTermoSemAcento() {
        assertThat(buscar("ficcao")).extracting(Produto::getTitulo).containsExactly("Ficção brasileira");
    }

    @Test
    void encontraAutorAcentuadoComTermoAcentuadoOuSemAcento() {
        assertThat(buscar("joao")).hasSize(1);
        assertThat(buscar("João")).hasSize(1);
        assertThat(buscar("ficcao joao")).hasSize(1);
    }

    @Test
    void caracteresEspeciaisNaoFuncionamComoCuringasDaConsulta() {
        assertThat(buscar("%_!!")).isEmpty();
    }

    private List<Produto> buscar(String termo) {
        return produtoRepository.findAll(
                ProdutoSpecification.disponivel().and(ProdutoSpecification.tituloOuAutorContem(termo)),
                PageRequest.of(0, 10)).getContent();
    }
}
