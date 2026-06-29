package com.vitral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.AccountResponse;
import com.vitral.dto.AtualizarPerfilRequest;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.mapper.AccountMapper;
import com.vitral.repository.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountResponse atualizarPerfil(Account account, AtualizarPerfilRequest request) {
        account.setName(request.name());
        account.setFotoUrl(request.fotoUrl());
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Transactional
    public MensagemResponse excluirConta(Account account) {
        accountRepository.delete(account);
        return new MensagemResponse("Conta excluida com sucesso");
    }
}
