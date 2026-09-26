package com.vitral.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.OffsetDateTime;

import com.vitral.dto.AdminAccountResponse;
import com.vitral.dto.AdminAuditResponse;
import com.vitral.dto.AdminDashboardResponse;
import com.vitral.dto.AdminStatusRequest;
import com.vitral.dto.PageResponse;
import com.vitral.dto.AdminPedidoResponse;
import com.vitral.dto.AdminProdutoResponse;
import com.vitral.dto.AdminSeboResponse;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusPedido;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.security.AccountUserDetails;
import com.vitral.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<PageResponse<AdminAccountResponse>> listarUsuarios(
            @RequestParam(required = false) String termo, @RequestParam(required = false) AccountType tipo,
            @RequestParam(required = false) Boolean ativo, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(adminService.listarContas(termo, tipo, ativo, pageable)));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<AdminAccountResponse> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.buscarConta(id));
    }

    @PutMapping("/usuarios/{id}/status")
    public ResponseEntity<AdminAccountResponse> alterarStatusUsuario(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id, @Valid @RequestBody AdminStatusRequest request) {
        return ResponseEntity.ok(adminService.alterarStatusConta(principal.getAccount(), id, request.ativo()));
    }

    @GetMapping("/produtos")
    public ResponseEntity<PageResponse<AdminProdutoResponse>> listarProdutos(@RequestParam(required = false) String termo,
            @RequestParam(required = false) Long seboId, @RequestParam(required = false) Boolean ativo, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(adminService.listarProdutos(termo, seboId, ativo, pageable)));
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<AdminProdutoResponse> buscarProduto(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.buscarProduto(id));
    }

    @PutMapping("/produtos/{id}/status")
    public ResponseEntity<AdminProdutoResponse> alterarStatusProduto(@AuthenticationPrincipal AccountUserDetails principal,
            @PathVariable Long id, @Valid @RequestBody AdminStatusRequest request) {
        return ResponseEntity.ok(adminService.alterarStatusProduto(principal.getAccount(), id, request.ativo()));
    }

    @GetMapping("/sebos")
    public ResponseEntity<PageResponse<AdminSeboResponse>> listarSebos(@RequestParam(required = false) String termo,
            @RequestParam(required = false) StatusVerificacaoSebo status, Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(adminService.listarSebos(termo, status, pageable)));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<PageResponse<AdminPedidoResponse>> listarPedidos(
            @RequestParam(required = false) Long id, @RequestParam(required = false) StatusPedido status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(adminService.listarPedidos(id, status, inicio, fim, pageable)));
    }

    @GetMapping("/auditoria")
    public ResponseEntity<PageResponse<AdminAuditResponse>> listarAuditoria(
            @RequestParam(required = false) Long adminId, @RequestParam(required = false) String acao,
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fim,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(
                adminService.listarAuditoria(adminId, acao, entidade, inicio, fim, pageable)));
    }
}
