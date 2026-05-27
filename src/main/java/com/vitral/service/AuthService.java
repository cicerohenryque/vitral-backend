package com.vitral.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.AuthResponse;
import com.vitral.dto.LoginRequest;
import com.vitral.dto.RegisterRequest;
import com.vitral.entity.Account;
import com.vitral.exception.ConflictException;
import com.vitral.mapper.AccountMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AccountMapper accountMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email ja cadastrado");
        }

        Account account = Account.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .type(request.type())
                .build();

        Account saved = accountRepository.save(account);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Account account = accountRepository.findByEmail(request.email())
                .orElseThrow(() -> new ConflictException("Conta nao encontrada"));
        return buildAuthResponse(account);
    }

    private AuthResponse buildAuthResponse(Account account) {
        String token = jwtService.generateToken(account.getEmail(), account.getType().name());
        return new AuthResponse(token, jwtService.getExpiration(), accountMapper.toResponse(account));
    }
}
