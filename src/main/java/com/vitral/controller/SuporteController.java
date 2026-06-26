package com.vitral.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vitral.dto.MensagemResponse;
import com.vitral.dto.SuporteRequest;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.SuporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/suporte")
@RequiredArgsConstructor
@Tag(name = "Suporte", description = "Envio de tickets de suporte ao Vitral")
public class SuporteController {

    private final SuporteService suporteService;

    @PostMapping
    @Operation(summary = "Envia um ticket de suporte para a equipe do Vitral")
    public ResponseEntity<MensagemResponse> enviar(
            @AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody SuporteRequest request) {
        suporteService.enviar(principal.getAccount(), request);
        return ResponseEntity.ok(new MensagemResponse("Mensagem enviada com sucesso. Nossa equipe entrara em contato em breve."));
    }
}
