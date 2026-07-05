package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import com.vitral.exception.BusinessException;
import com.vitral.entity.Account;
import com.vitral.entity.DocumentoUpload;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.repository.DocumentoUploadRepository;
import com.vitral.repository.SeboRepository;
import java.util.Optional;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class DocumentoUploadServiceTest {
    @TempDir Path tempDir;
    @Mock SeboRepository seboRepository;
    @Mock DocumentoUploadRepository metadataRepository;
    private Account account;
    private Sebo sebo;

    @BeforeEach
    void setup() {
        account = Account.builder().type(AccountType.SEBO).build();
        sebo = Sebo.builder().account(account).build();
        setId(account, 1L); setId(sebo, 2L);
        lenient().when(seboRepository.findByAccountId(1L)).thenReturn(Optional.of(sebo));
    }

    private DocumentoUploadService service(long limite) {
        return new DocumentoUploadService(tempDir.toString(), limite, seboRepository, metadataRepository);
    }

    @Test
    void aceitaPdfValido() {
        DocumentoUploadService service = service(10 * 1024 * 1024);
        var file = new MockMultipartFile("file", "contrato.pdf", "application/pdf", "%PDF-1.7 conteudo".getBytes());
        String url = service.armazenar(account, file);
        assertThat(url).startsWith("/api/v1/uploads/documents/").endsWith(".pdf");
        assertThat(Files.exists(tempDir.resolve(url.substring(url.lastIndexOf('/') + 1)))).isTrue();
    }

    @Test
    void aceitaImagemPngValida() {
        DocumentoUploadService service = service(1024);
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a, 1};
        var file = new MockMultipartFile("file", "documento.png", "image/png", png);
        assertThat(service.armazenar(account, file)).endsWith(".png");
    }

    @Test
    void rejeitaTipoDeclaradoComConteudoFalso() {
        DocumentoUploadService service = service(1024);
        var file = new MockMultipartFile("file", "falso.pdf", "application/pdf", "nao e pdf".getBytes());
        assertThatThrownBy(() -> service.armazenar(account, file)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo de arquivo invalido");
    }

    @Test
    void rejeitaDocumentoAcimaDoLimite() {
        DocumentoUploadService service = service(4);
        var file = new MockMultipartFile("file", "contrato.pdf", "application/pdf", "%PDF-".getBytes());
        assertThatThrownBy(() -> service.armazenar(account, file)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("tamanho maximo");
    }

    @Test
    void rejeitaExtensaoIncompativel() {
        var file = new MockMultipartFile("file", "contrato.txt", "application/pdf", "%PDF-1.7".getBytes());
        assertThatThrownBy(() -> service(1024).armazenar(account, file)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("extensao");
    }

    @Test
    void impedeOutroSeboDeLerDocumento() {
        Account outro = Account.builder().type(AccountType.SEBO).build(); setId(outro, 9L);
        DocumentoUpload metadata = DocumentoUpload.builder().sebo(sebo).nomeInterno("arquivo.pdf")
                .nomeOriginal("arquivo.pdf").mimeType("application/pdf").tamanhoBytes(10L).build();
        when(metadataRepository.findByNomeInterno("arquivo.pdf")).thenReturn(Optional.of(metadata));
        assertThatThrownBy(() -> service(1024).carregar(outro, "arquivo.pdf"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Acesso negado");
    }

    @Test
    void impedePathTraversal() {
        DocumentoUpload metadata = DocumentoUpload.builder().sebo(sebo).nomeInterno("../arquivo.pdf")
                .nomeOriginal("arquivo.pdf").mimeType("application/pdf").tamanhoBytes(10L).build();
        when(metadataRepository.findByNomeInterno("../arquivo.pdf")).thenReturn(Optional.of(metadata));
        assertThatThrownBy(() -> service(1024).carregar(account, "../arquivo.pdf"))
                .isInstanceOf(com.vitral.exception.ResourceNotFoundException.class);
    }

    private void setId(Object entity, Long id) {
        try {
            var field = com.vitral.entity.BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true); field.set(entity, id);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
