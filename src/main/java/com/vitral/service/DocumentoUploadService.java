package com.vitral.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;

@Service
public class DocumentoUploadService {
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/webp");
    private static final Map<String, String> EXTENSOES = Map.of(
            "application/pdf", ".pdf", "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final Path diretorio;
    private final long tamanhoMaximo;

    public DocumentoUploadService(
            @Value("${app.upload.document-dir:uploads/documents}") String diretorio,
            @Value("${app.upload.document-max-size-bytes:10485760}") long tamanhoMaximo) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
        this.tamanhoMaximo = tamanhoMaximo;
    }

    public String armazenar(MultipartFile arquivo) {
        String tipo = validar(arquivo);
        try {
            Files.createDirectories(diretorio);
            String nome = UUID.randomUUID() + EXTENSOES.get(tipo);
            Path destino = diretorio.resolve(nome).normalize();
            if (!destino.startsWith(diretorio)) throw erro("Nome de arquivo invalido.");
            try (InputStream input = arquivo.getInputStream()) {
                Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/api/v1/uploads/documents/" + nome;
        } catch (IOException e) {
            throw erro("Nao foi possivel salvar o documento.");
        }
    }

    public Resource carregar(String nome) {
        try {
            Path arquivo = diretorio.resolve(nome).normalize();
            if (!arquivo.startsWith(diretorio) || !Files.isRegularFile(arquivo)) {
                throw new ResourceNotFoundException("Documento nao encontrado.");
            }
            return new UrlResource(arquivo.toUri());
        } catch (IOException e) {
            throw new ResourceNotFoundException("Documento nao encontrado.");
        }
    }

    public String contentType(String nome) {
        String n = nome.toLowerCase(Locale.ROOT);
        if (n.endsWith(".pdf")) return "application/pdf";
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    private String validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) throw erro("Selecione um documento para upload.");
        if (arquivo.getSize() > tamanhoMaximo) throw erro("O documento excede o tamanho maximo permitido.");
        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw erro("Formato invalido. Use PDF, JPG, PNG ou WEBP.");
        }
        try {
            byte[] inicio = arquivo.getInputStream().readNBytes(12);
            String detectado = detectarTipo(inicio);
            if (!arquivo.getContentType().equals(detectado)) {
                throw erro("O conteudo do arquivo nao corresponde ao formato informado.");
            }
            return detectado;
        } catch (IOException e) {
            throw erro("Nao foi possivel validar o documento.");
        }
    }

    private String detectarTipo(byte[] b) {
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') return "application/pdf";
        if (b.length >= 3 && (b[0] & 0xff) == 0xff && (b[1] & 0xff) == 0xd8 && (b[2] & 0xff) == 0xff) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 0xff) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0d && b[5] == 0x0a && b[6] == 0x1a && b[7] == 0x0a) return "image/png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        throw erro("Conteudo de arquivo invalido. Use PDF, JPG, PNG ou WEBP.");
    }

    private BusinessException erro(String mensagem) {
        return new BusinessException(mensagem, HttpStatus.BAD_REQUEST);
    }
}
