package com.vitral.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "CRUD do catalogo do sebo e consulta publica")
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Cadastra um produto no catalogo do sebo autenticado")
    public ResponseEntity<ProdutoResponse> criar(@AuthenticationPrincipal AccountUserDetails principal,
            @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.criar(principal.getAccount(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Atualiza um produto do sebo autenticado")
    public ResponseEntity<ProdutoResponse> atualizar(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(produtoService.atualizar(principal.getAccount(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SEBO')")
    @Operation(summary = "Remove logicamente um produto (soft delete) do sebo autenticado")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id) {
        produtoService.remover(principal.getAccount(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta publica de um produto ativo pelo id")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/sebo/{seboId}")
    @Operation(summary = "Lista paginada do catalogo ativo de um sebo")
    public ResponseEntity<Page<ProdutoResponse>> listarPorSebo(@PathVariable Long seboId, Pageable pageable) {
        return ResponseEntity.ok(produtoService.listarPorSebo(seboId, pageable));
    }
}
