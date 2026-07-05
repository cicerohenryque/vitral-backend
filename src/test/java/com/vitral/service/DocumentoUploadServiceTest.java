package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.vitral.exception.BusinessException;

class DocumentoUploadServiceTest {
    @TempDir Path tempDir;

    @Test
    void aceitaPdfValido() {
        DocumentoUploadService service = new DocumentoUploadService(tempDir.toString(), 10 * 1024 * 1024);
        var file = new MockMultipartFile("file", "contrato.pdf", "application/pdf", "%PDF-1.7 conteudo".getBytes());
        String url = service.armazenar(file);
        assertThat(url).startsWith("/api/v1/uploads/documents/").endsWith(".pdf");
        assertThat(Files.exists(tempDir.resolve(url.substring(url.lastIndexOf('/') + 1)))).isTrue();
    }

    @Test
    void aceitaImagemPngValida() {
        DocumentoUploadService service = new DocumentoUploadService(tempDir.toString(), 1024);
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a, 1};
        var file = new MockMultipartFile("file", "documento.png", "image/png", png);
        assertThat(service.armazenar(file)).endsWith(".png");
    }

    @Test
    void rejeitaTipoDeclaradoComConteudoFalso() {
        DocumentoUploadService service = new DocumentoUploadService(tempDir.toString(), 1024);
        var file = new MockMultipartFile("file", "falso.pdf", "application/pdf", "nao e pdf".getBytes());
        assertThatThrownBy(() -> service.armazenar(file)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conteudo de arquivo invalido");
    }

    @Test
    void rejeitaDocumentoAcimaDoLimite() {
        DocumentoUploadService service = new DocumentoUploadService(tempDir.toString(), 4);
        var file = new MockMultipartFile("file", "contrato.pdf", "application/pdf", "%PDF-".getBytes());
        assertThatThrownBy(() -> service.armazenar(file)).isInstanceOf(BusinessException.class)
                .hasMessageContaining("tamanho maximo");
    }
}
