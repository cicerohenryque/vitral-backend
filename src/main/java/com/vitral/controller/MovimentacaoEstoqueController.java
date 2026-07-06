package com.vitral.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vitral.dto.EstornarVendaFisicaRequest;
import com.vitral.dto.MovimentacaoEstoqueResponse;
import com.vitral.dto.PageResponse;
import com.vitral.dto.RegistrarVendaFisicaRequest;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movimentacoes-estoque")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SEBO')")
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService service;

    @PostMapping("/vendas-fisicas")
    public ResponseEntity<MovimentacaoEstoqueResponse> registrarVendaFisica(
            @AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody RegistrarVendaFisicaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarVendaFisica(principal.getAccount(), request));
    }

    @PostMapping("/{id}/estorno")
    public ResponseEntity<MovimentacaoEstoqueResponse> estornar(
            @AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody EstornarVendaFisicaRequest request) {
        return ResponseEntity.ok(service.estornarVendaFisica(principal.getAccount(), id, request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MovimentacaoEstoqueResponse>> listar(
            @AuthenticationPrincipal AccountUserDetails principal, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(service.listar(principal.getAccount(), pageable)));
    }
}
