package com.vitral.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.MensagemResponse;
import com.vitral.dto.SeboRequest;
import com.vitral.dto.SeboResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.SeboRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeboService {

    private final SeboRepository seboRepository;
    private final SeboMapper seboMapper;
    private final AccountRepository accountRepository;

    @Transactional
    public SeboResponse criar(Account account, SeboRequest request) {
        if (account.getType() != AccountType.SEBO) {
            throw new BusinessException("Apenas contas do tipo SEBO podem criar perfil de sebo", HttpStatus.FORBIDDEN);
        }
        if (seboRepository.existsByAccountId(account.getId())) {
            throw new ConflictException("Sebo ja cadastrado para esta conta");
        }
        Sebo sebo = Sebo.builder()
                .account(account)
                .descricao(request.descricao())
                .telefone(request.telefone())
                .cep(request.cep())
                .logradouro(request.logradouro())
                .cidade(request.cidade())
                .uf(request.uf())
                .horarioFuncionamento(request.horarioFuncionamento())
                .fotoUrl(request.fotoUrl())
                .build();
        Sebo salvo = seboRepository.save(sebo);
        return seboMapper.toResponse(salvo);
    }

    @Transactional
    public SeboResponse atualizarMeuSebo(Account account, SeboRequest request) {
        Sebo sebo = seboRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado para esta conta"));
        sebo.setDescricao(request.descricao());
        sebo.setTelefone(request.telefone());
        sebo.setCep(request.cep());
        sebo.setLogradouro(request.logradouro());
        sebo.setCidade(request.cidade());
        sebo.setUf(request.uf());
        sebo.setHorarioFuncionamento(request.horarioFuncionamento());
        sebo.setFotoUrl(request.fotoUrl());
        return seboMapper.toResponse(sebo);
    }

    @Transactional(readOnly = true)
    public SeboResponse buscarPorId(Long id) {
        Sebo sebo = seboRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado"));
        return seboMapper.toResponse(sebo);
    }

    @Transactional(readOnly = true)
    public Sebo buscarEntidadePorAccount(Long accountId) {
        return seboRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado para esta conta"));
    }

    @Transactional
    public MensagemResponse excluirConta(Account account) {
        accountRepository.delete(account);
        return new MensagemResponse("Conta do sebo excluida com sucesso");
    }
}
