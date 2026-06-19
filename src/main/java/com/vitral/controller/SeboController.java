package com.vitral.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vitral.dto.MensagemResponse;
import com.vitral.dto.SeboRequest;
import com.vitral.dto.SeboResponse;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.SeboService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sebos")
@RequiredArgsConstructor
@Tag(name = "Sebos", description = "Cadastro e consulta de sebos")
public class SeboController {

    private final SeboService seboService;

    @PostMapping
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Cria o perfil do sebo vinculado a conta autenticada")
    public ResponseEntity<SeboResponse> criar(@AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody SeboRequest request) {
        SeboResponse response = seboService.criar(principal.getAccount(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Atualiza o perfil do sebo da conta autenticada")
    public ResponseEntity<SeboResponse> atualizarMeuSebo(@AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody SeboRequest request) {
        return ResponseEntity.ok(seboService.atualizarMeuSebo(principal.getAccount(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta publica de um sebo pelo id")
    public ResponseEntity<SeboResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(seboService.buscarPorId(id));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Exclui permanentemente a conta do sebo autenticado e todos os seus dados")
    public ResponseEntity<MensagemResponse> excluirConta(
            @AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(seboService.excluirConta(principal.getAccount()));
    }
}
