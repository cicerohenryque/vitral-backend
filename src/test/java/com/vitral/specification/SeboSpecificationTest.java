package com.vitral.specification;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.vitral.entity.Account;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.repository.SeboRepository;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
class SeboSpecificationTest {

    @Autowired private SeboRepository seboRepository;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void cadastrarSeboAcentuado() {
        Account account = Account.builder()
                .name("Sebo João e Música")
                .username("sebo-busca")
                .email("sebo-busca@teste.local")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .ativo(true)
                .build();
        entityManager.persist(account);
        entityManager.persist(Sebo.builder()
                .account(account)
                .cidade("São Luís")
                .statusVerificacao(StatusVerificacaoSebo.VERIFICADO)
                .build());
        entityManager.flush();
    }

    @Test
    void encontraNomeECidadeIndependentementeDeAcentos() {
        assertThat(seboRepository.findAll(SeboSpecification.nomeContem("joao musica"))).hasSize(1);
        assertThat(seboRepository.findAll(SeboSpecification.cidadeContem("sao luis"))).hasSize(1);
    }

    @Test
    void caracteresEspeciaisNaoFuncionamComoCuringas() {
        assertThat(seboRepository.findAll(SeboSpecification.nomeContem("%_!!"))).isEmpty();
    }
}
