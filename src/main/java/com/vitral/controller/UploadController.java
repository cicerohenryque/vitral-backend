package com.vitral.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vitral.dto.UploadResponse;
import com.vitral.service.UploadService;
import com.vitral.service.DocumentoUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
@Tag(name = "Uploads", description = "Upload e consulta publica de imagens")
public class UploadController {

    private final UploadService uploadService;
    private final DocumentoUploadService documentoUploadService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Faz upload de uma imagem JPG, PNG ou WEBP")
    public ResponseEntity<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new UploadResponse(uploadService.storeImage(file)));
    }

    @GetMapping("/images/{filename}")
    @Operation(summary = "Consulta publica de uma imagem enviada")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource image = uploadService.loadImage(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .contentType(MediaType.parseMediaType(uploadService.contentType(filename)))
                .body(image);
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Faz upload privado de documento PDF, JPG, PNG ou WEBP")
    public ResponseEntity<UploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new UploadResponse(documentoUploadService.armazenar(file)));
    }

    @GetMapping("/documents/{filename}")
    @PreAuthorize("hasAnyRole('SEBO','ADMIN')")
    @Operation(summary = "Consulta autenticada de documento de verificacao")
    public ResponseEntity<Resource> getDocument(@PathVariable String filename) {
        Resource document = documentoUploadService.carregar(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(documentoUploadService.contentType(filename)))
                .body(document);
    }
}
