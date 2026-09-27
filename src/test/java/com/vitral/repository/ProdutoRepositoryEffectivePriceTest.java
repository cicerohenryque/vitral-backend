package com.vitral.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.vitral.entity.Account;
import com.vitral.entity.Oferta;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.CondicaoProduto;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoRepositoryEffectivePriceTest {

    @Autowired private AccountRepository accountRepository;
    @Autowired private SeboRepository seboRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private OfertaRepository ofertaRepository;

    @Test
    void ordenaPeloPrecoPromocionalSomenteQuandoAOfertaEstaVigente() {
        Account account = accountRepository.save(Account.builder()
                .name("Sebo teste")
                .username("sebo-preco-efetivo")
                .email("preco-efetivo@vitral.test")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .build());
        Sebo sebo = seboRepository.save(Sebo.builder().account(account).build());

        Produto semOferta = produtoRepository.save(produto(sebo, "Sem oferta", "20.00"));
        Produto comOferta = produtoRepository.save(produto(sebo, "Com oferta", "100.00"));
        Produto ofertaExpirada = produtoRepository.save(produto(sebo, "Oferta expirada", "15.00"));

        OffsetDateTime agora = OffsetDateTime.now();
        ofertaRepository.save(Oferta.builder()
                .produto(comOferta)
                .precoPromocional(new BigDecimal("10.00"))
                .inicioEm(agora.minusDays(1))
                .fimEm(agora.plusDays(1))
                .build());
        ofertaRepository.save(Oferta.builder()
                .produto(ofertaExpirada)
                .precoPromocional(new BigDecimal("1.00"))
                .fimEm(agora.minusDays(1))
                .build());
        produtoRepository.flush();
        ofertaRepository.flush();

        var resultado = produtoRepository.findAll(
                PageRequest.of(0, 10, Sort.by("precoEfetivo").ascending()));

        assertThat(resultado.getContent())
                .extracting(Produto::getId)
                .containsExactly(comOferta.getId(), ofertaExpirada.getId(), semOferta.getId());
    }

    private Produto produto(Sebo sebo, String titulo, String preco) {
        return Produto.builder()
                .sebo(sebo)
                .titulo(titulo)
                .preco(new BigDecimal(preco))
                .estoque(1)
                .condicao(CondicaoProduto.USADO)
                .build();
    }
}
