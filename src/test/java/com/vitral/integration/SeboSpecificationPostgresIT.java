package com.vitral.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.vitral.entity.Account;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.repository.SeboRepository;
import com.vitral.specification.SeboSpecification;

import jakarta.persistence.EntityManager;

@DataJpaTest(properties = { "spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop" })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SeboSpecificationPostgresIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired private EntityManager entityManager;
    @Autowired private SeboRepository seboRepository;

    @Test
    void filtraRetanguloGeograficoNoPostgresqlReal() {
        persistirSebo("Sebo próximo", "sebo-proximo", -7.1219, -34.8850);
        persistirSebo("Sebo distante", "sebo-distante", -23.5505, -46.6333);
        entityManager.flush();
        entityManager.clear();

        var encontrados = seboRepository.findAll(
                SeboSpecification.dentroDoRetangulo(-7.35, -6.88, -35.10, -34.63));

        assertThat(encontrados).extracting(sebo -> sebo.getAccount().getName())
                .containsExactly("Sebo próximo");
    }

    private void persistirSebo(String nome, String username, double latitude, double longitude) {
        Account account = Account.builder().name(nome).username(username).email(username + "@teste.local")
                .passwordHash("hash").type(AccountType.SEBO).emailVerificado(true).ativo(true).build();
        entityManager.persist(account);
        entityManager.persist(Sebo.builder().account(account)
                .statusVerificacao(StatusVerificacaoSebo.VERIFICADO)
                .latitude(latitude).longitude(longitude).build());
    }
}
