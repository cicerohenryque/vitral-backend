package com.vitral.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import com.vitral.exception.BusinessException;

class UploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve salvar imagem e retornar URL publica")
    void storeImage_sucesso() throws Exception {
        UploadService service = new UploadService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "capa.png", "image/png", "imagem".getBytes());

        String url = service.storeImage(file);

        assertThat(url).startsWith("/api/v1/uploads/images/").endsWith(".png");
        String filename = url.substring(url.lastIndexOf('/') + 1);
        assertThat(Files.exists(tempDir.resolve(filename))).isTrue();
        assertThat(service.loadImage(filename).exists()).isTrue();
        assertThat(service.contentType(filename)).isEqualTo("image/png");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void storeImage_vazio_lancaErro() {
        UploadService service = new UploadService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "vazio.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.storeImage(file))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Deve rejeitar formato diferente de imagem")
    void storeImage_formatoInvalido_lancaErro() {
        UploadService service = new UploadService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "nota.txt", "text/plain", "texto".getBytes());

        assertThatThrownBy(() -> service.storeImage(file))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getMessage()).contains("Formato de imagem invalido"));
    }
}
