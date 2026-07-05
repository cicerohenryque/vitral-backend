package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vitral.dto.DocumentoVerificacaoSeboRequest;
import com.vitral.entity.Account;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.TipoDocumentoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.AuditoriaVerificacaoSeboRepository;
import com.vitral.repository.DocumentoUploadRepository;
import com.vitral.repository.DocumentoVerificacaoSeboRepository;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;

@ExtendWith(MockitoExtension.class)
class VerificacaoSeboDocumentoServiceTest {
    @Mock SeboRepository seboRepository;
    @Mock DocumentoVerificacaoSeboRepository documentoRepository;
    @Mock AuditoriaVerificacaoSeboRepository auditoriaRepository;
    @Mock ProdutoRepository produtoRepository;
    @Mock SeboMapper seboMapper;
    @Mock DocumentoUploadRepository uploadRepository;
    @InjectMocks VerificacaoSeboService service;
    private Account account;

    @BeforeEach
    void setup() {
        account = Account.builder().build(); setId(account, 1L);
        when(seboRepository.findByAccountId(1L)).thenReturn(Optional.of(Sebo.builder().account(account).build()));
    }

    @Test
    void rejeitaUrlExterna() {
        var request = new DocumentoVerificacaoSeboRequest(TipoDocumentoSebo.CONTRATO_SOCIAL, "https://externo/arquivo.pdf");
        assertThatThrownBy(() -> service.enviarDocumento(account, request)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("URL de documento gerada pelo backend");
    }

    @Test
    void rejeitaArquivoDeOutroSebo() {
        String url = "/api/v1/uploads/documents/arquivo.pdf";
        when(uploadRepository.existsByNomeInternoAndSeboAccountId("arquivo.pdf", 1L)).thenReturn(false);
        var request = new DocumentoVerificacaoSeboRequest(TipoDocumentoSebo.CONTRATO_SOCIAL, url);
        assertThatThrownBy(() -> service.enviarDocumento(account, request)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao pertence");
    }

    private void setId(Object entity, Long id) {
        try {
            var field = com.vitral.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true); field.set(entity, id);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
