package com.vitral.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AuthResponse;
import com.vitral.dto.LoginRequest;
import com.vitral.dto.MensagemResponse;
import com.vitral.dto.RegisterRequest;
import com.vitral.dto.RecuperarSenhaRequest;
import com.vitral.dto.RedefinirSenhaRequest;
import com.vitral.mapper.AccountMapper;
import com.vitral.security.AccountUserDetails;
import com.vitral.security.JwtService;
import com.vitral.security.TokenBlacklistService;
import com.vitral.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Cadastro, login, confirmacao de e-mail e dados da conta autenticada")
public class AuthController {

    private final AuthService authService;
    private final AccountMapper accountMapper;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    @Operation(summary = "Cria uma conta (Sebo ou Usuario) e envia e-mail de confirmacao")
    public ResponseEntity<MensagemResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica e retorna o token de acesso — requer e-mail confirmado")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/confirmar")
    @Operation(summary = "Confirma o cadastro via token recebido por e-mail")
    public ResponseEntity<MensagemResponse> confirmar(@RequestParam String token) {
        return ResponseEntity.ok(authService.confirmarEmail(token));
    }

    @PostMapping("/recuperar-senha")
    @Operation(summary = "Envia um link temporario para redefinicao de senha")
    public ResponseEntity<MensagemResponse> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        return ResponseEntity.ok(authService.solicitarRecuperacaoSenha(request));
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefine a senha usando um token temporario")
    public ResponseEntity<MensagemResponse> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        return ResponseEntity.ok(authService.redefinirSenha(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados da conta autenticada")
    public ResponseEntity<AccountResponse> me(@AuthenticationPrincipal AccountUserDetails principal) {
        return ResponseEntity.ok(accountMapper.toResponse(principal.getAccount()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessao e invalida o token JWT no servidor")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtService.isValid(token)) {
                tokenBlacklistService.revoke(token, jwtService.extractExpiration(token));
            }
        }
        return ResponseEntity.noContent().build();
    }
}
