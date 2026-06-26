package com.vitral.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vitral.dto.MensagemChatRequest;
import com.vitral.dto.MensagemChatResponse;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.MensagemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mensagens")
@RequiredArgsConstructor
@Tag(name = "Mensagens", description = "Troca de mensagens entre usuarios e sebos")
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping
    @Operation(summary = "Envia uma mensagem para outro usuario ou sebo")
    public ResponseEntity<MensagemChatResponse> enviar(
            @AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody MensagemChatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensagemService.enviar(principal.getAccount(), request));
    }

    @GetMapping
    @Operation(summary = "Lista todas as mensagens do usuario autenticado")
    public ResponseEntity<Page<MensagemChatResponse>> listar(
            @AuthenticationPrincipal AccountUserDetails principal,
            Pageable pageable) {
        return ResponseEntity.ok(mensagemService.listar(principal.getAccount(), pageable));
    }

    @GetMapping("/conversa/{accountId}")
    @Operation(summary = "Lista a conversa com um usuario especifico")
    public ResponseEntity<Page<MensagemChatResponse>> listarConversa(
            @AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long accountId,
            Pageable pageable) {
        return ResponseEntity.ok(mensagemService.listarConversa(principal.getAccount(), accountId, pageable));
    }
}
