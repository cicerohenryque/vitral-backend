package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import com.vitral.dto.AdminSeboResponse;
import com.vitral.entity.Account;
import com.vitral.entity.AdminAudit;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.exception.BusinessException;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.AdminAuditRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private SeboRepository seboRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private AdminAuditRepository adminAuditRepository;
    @Mock private SeboGeocodingService seboGeocodingService;
    @InjectMocks private AdminService adminService;

    @Test
    void geocodificarSebo_atualizaLocalizacaoERegistraAuditoria() {
        Account admin = account(1L, "Admin", AccountType.ADMIN);
        Account responsavel = account(2L, "Sebo Central", AccountType.SEBO);
        Sebo sebo = Sebo.builder().account(responsavel).build();
        ReflectionTestUtils.setField(sebo, "id", 7L);
        when(seboRepository.findById(7L)).thenReturn(Optional.of(sebo));
        when(produtoRepository.contarPorSeboEStatus(List.of(7L))).thenReturn(List.of());
        org.mockito.Mockito.doAnswer(invocation -> {
            Sebo alvo = invocation.getArgument(0);
            alvo.setLatitude(-7.12);
            alvo.setLongitude(-34.88);
            return null;
        }).when(seboGeocodingService).geocodificar(sebo);

        AdminSeboResponse response = adminService.geocodificarSebo(admin, 7L);

        assertThat(response.localizacaoDisponivel()).isTrue();
        verify(seboRepository).save(sebo);
        ArgumentCaptor<AdminAudit> audit = ArgumentCaptor.forClass(AdminAudit.class);
        verify(adminAuditRepository).save(audit.capture());
        assertThat(audit.getValue().getAcao()).isEqualTo("REGEOCODIFICOU");
        assertThat(audit.getValue().getEntidade()).isEqualTo("SEBO");
        assertThat(audit.getValue().getEntidadeId()).isEqualTo(7L);
        assertThat(audit.getValue().getDescricao()).isEqualTo("Localização do sebo atualizada.");
    }

    @Test
    void geocodificarSebo_inexistenteRetornaNotFound() {
        Account admin = account(1L, "Admin", AccountType.ADMIN);
        when(seboRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.geocodificarSebo(admin, 99L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Account account(Long id, String name, AccountType type) {
        Account account = Account.builder().name(name).username(name.toLowerCase().replace(' ', '-'))
                .email(id + "@vitral.test").passwordHash("hash").type(type).build();
        ReflectionTestUtils.setField(account, "id", id);
        return account;
    }
}
