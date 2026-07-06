package com.vitral.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import com.vitral.dto.EstornarVendaFisicaRequest;
import com.vitral.dto.MovimentacaoEstoqueResponse;
import com.vitral.dto.RegistrarVendaFisicaRequest;
import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.TipoMovimentacaoEstoque;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.MovimentacaoEstoqueService;

@ExtendWith(MockitoExtension.class)
class MovimentacaoEstoqueControllerTest {

    @Mock
    private MovimentacaoEstoqueService service;

    @InjectMocks
    private MovimentacaoEstoqueController controller;

    @Test
    void registrarVendaFisicaRetornaCreated() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        RegistrarVendaFisicaRequest request = new RegistrarVendaFisicaRequest(30L, 2, BigDecimal.valueOf(45), "balcao");
        MovimentacaoEstoqueResponse response = movimentacao(TipoMovimentacaoEstoque.VENDA_FISICA);
        when(service.registrarVendaFisica(account, request)).thenReturn(response);

        var result = controller.registrarVendaFisica(principal, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isSameAs(response);
        verify(service).registrarVendaFisica(account, request);
    }

    @Test
    void estornarVendaFisicaRepassaMotivo() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        EstornarVendaFisicaRequest request = new EstornarVendaFisicaRequest("cliente desistiu");
        MovimentacaoEstoqueResponse response = movimentacao(TipoMovimentacaoEstoque.ESTORNO);
        when(service.estornarVendaFisica(account, 40L, request)).thenReturn(response);

        var result = controller.estornar(principal, 40L, request);

        assertThat(result.getBody()).isSameAs(response);
        verify(service).estornarVendaFisica(account, 40L, request);
    }

    @Test
    void listarConvertePageParaContratoEstavel() {
        Account account = account();
        AccountUserDetails principal = new AccountUserDetails(account);
        var pageable = PageRequest.of(0, 20);
        MovimentacaoEstoqueResponse response = movimentacao(TipoMovimentacaoEstoque.VENDA_FISICA);
        when(service.listar(account, pageable)).thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        var result = controller.listar(principal, pageable);

        assertThat(result.getBody().content()).containsExactly(response);
        assertThat(result.getBody().totalElements()).isEqualTo(1);
        verify(service).listar(account, pageable);
    }

    private Account account() {
        return Account.builder().type(AccountType.SEBO).email("sebo@vitral.com").passwordHash("hash").ativo(true).build();
    }

    private MovimentacaoEstoqueResponse movimentacao(TipoMovimentacaoEstoque tipo) {
        return new MovimentacaoEstoqueResponse(40L, 30L, 10L, 1L, null, tipo, 2, 5, 3,
                BigDecimal.valueOf(45), BigDecimal.valueOf(90), "balcao", OffsetDateTime.now());
    }
}
