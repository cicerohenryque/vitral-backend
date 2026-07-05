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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import com.vitral.dto.PageResponse;
import com.vitral.enumerations.StatusVerificacaoSebo;

import com.vitral.dto.MensagemResponse;
import com.vitral.dto.SeboRequest;
import com.vitral.dto.SeboResponse;
import com.vitral.dto.DocumentoVerificacaoSeboRequest;
import com.vitral.dto.DocumentoVerificacaoSeboResponse;
import com.vitral.dto.AuditoriaVerificacaoSeboResponse;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.SeboService;
import com.vitral.service.VerificacaoSeboService;
import java.util.List;

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
    private final VerificacaoSeboService verificacaoSeboService;

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

    @GetMapping("/me")
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Consulta o perfil do sebo vinculado a conta autenticada")
    public ResponseEntity<SeboResponse> buscarMeuSebo(@AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(seboService.buscarMeuSebo(principal.getAccount()));
    }

    @GetMapping("/verificacao/pendentes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista sebos aguardando verificacao")
    public ResponseEntity<PageResponse<SeboResponse>> listarPendentes(Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(seboService.listarPendentes(pageable)));
    }

    @PutMapping("/{id}/verificacao")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprova ou rejeita a verificacao de um sebo")
    public ResponseEntity<SeboResponse> revisarVerificacao(
            @PathVariable Long id,
            @RequestParam StatusVerificacaoSebo status,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(verificacaoSeboService.revisar(principal.getAccount(), id, status, motivo));
    }

    @PostMapping("/me/documentos")
    @PreAuthorize("hasRole('SEBO')")
    public ResponseEntity<DocumentoVerificacaoSeboResponse> enviarDocumento(
            @AuthenticationPrincipal AccountUserDetails principal, @Valid @RequestBody DocumentoVerificacaoSeboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verificacaoSeboService.enviarDocumento(principal.getAccount(), request));
    }

    @GetMapping("/me/documentos")
    @PreAuthorize("hasRole('SEBO')")
    public ResponseEntity<List<DocumentoVerificacaoSeboResponse>> listarMeusDocumentos(@AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(verificacaoSeboService.listarMeusDocumentos(principal.getAccount()));
    }

    @GetMapping("/{id}/documentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentoVerificacaoSeboResponse>> listarDocumentos(@PathVariable Long id) {
        return ResponseEntity.ok(verificacaoSeboService.listarDocumentos(id));
    }

    @GetMapping("/{id}/verificacao/auditoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditoriaVerificacaoSeboResponse>> listarAuditoria(@PathVariable Long id) {
        return ResponseEntity.ok(verificacaoSeboService.listarAuditoria(id));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Exclui permanentemente a conta do sebo autenticado e todos os seus dados")
    public ResponseEntity<MensagemResponse> excluirConta(
            @AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(seboService.excluirConta(principal.getAccount()));
    }
}
