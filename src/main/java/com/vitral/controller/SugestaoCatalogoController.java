package com.vitral.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vitral.dto.PageResponse;
import com.vitral.dto.AprovarSugestaoCatalogoRequest;
import com.vitral.dto.RejeitarSugestaoCatalogoRequest;
import com.vitral.dto.SugestaoCatalogoRequest;
import com.vitral.dto.SugestaoCatalogoResponse;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.SugestaoCatalogoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/sugestoes-catalogo")
@RequiredArgsConstructor
public class SugestaoCatalogoController {

    private final SugestaoCatalogoService service;

    @PostMapping
    @PreAuthorize("hasRole('USUARIO')")
    public ResponseEntity<SugestaoCatalogoResponse> criar(@AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody SugestaoCatalogoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(principal.getAccount(), request));
    }

    @GetMapping("/minhas")
    @PreAuthorize("hasRole('USUARIO')")
    public PageResponse<SugestaoCatalogoResponse> listarMinhas(@AuthenticationPrincipal AccountUserDetails principal,
            Pageable pageable) {
        return PageResponse.from(service.listarMinhas(principal.getAccount(), pageable));
    }

    @GetMapping("/recebidas")
    @PreAuthorize("hasRole('SEBO')")
    public PageResponse<SugestaoCatalogoResponse> listarRecebidas(@AuthenticationPrincipal AccountUserDetails principal,
            Pageable pageable) {
        return PageResponse.from(service.listarRecebidas(principal.getAccount(), pageable));
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('SEBO')")
    public SugestaoCatalogoResponse aprovar(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id, @Valid @RequestBody(required = false) AprovarSugestaoCatalogoRequest request) {
        return service.aprovar(principal.getAccount(), id, request);
    }

    @PutMapping("/{id}/rejeitar")
    @PreAuthorize("hasRole('SEBO')")
    public SugestaoCatalogoResponse rejeitar(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id, @Valid @RequestBody(required = false) RejeitarSugestaoCatalogoRequest request) {
        return service.rejeitar(principal.getAccount(), id, request == null ? null : request.motivo());
    }
}
