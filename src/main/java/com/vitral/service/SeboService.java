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
import com.vitral.enumerations.StatusVerificacaoSebo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.SeboRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeboService {

    private final SeboRepository seboRepository;
    private final SeboMapper seboMapper;
    private final AnonimizacaoContaService anonimizacaoContaService;

    @Transactional
    public SeboResponse criar(Account account, SeboRequest request) {
        if (account.getType() != AccountType.SEBO) {
            throw new BusinessException("Apenas contas do tipo SEBO podem criar perfil de sebo", HttpStatus.FORBIDDEN);
        }
        if (seboRepository.existsByAccountId(account.getId())) {
            throw new ConflictException("Sebo ja cadastrado para esta conta");
        }
        if (seboRepository.existsByCnpj(request.cnpj())) {
            throw new ConflictException("CNPJ ja cadastrado");
        }
        Sebo sebo = Sebo.builder()
                .account(account)
                .descricao(request.descricao())
                .telefone(request.telefone())
                .cnpj(request.cnpj())
                .statusVerificacao(StatusVerificacaoSebo.PENDENTE)
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
        if (!java.util.Objects.equals(sebo.getCnpj(), request.cnpj())) {
            if (seboRepository.existsByCnpj(request.cnpj())) {
                throw new ConflictException("CNPJ ja cadastrado");
            }
            sebo.setCnpj(request.cnpj());
            sebo.setStatusVerificacao(StatusVerificacaoSebo.PENDENTE);
            sebo.setVerificadoEm(null);
        }
        sebo.setFotoUrl(request.fotoUrl());
        return seboMapper.toResponse(sebo);
    }

    @Transactional(readOnly = true)
    public SeboResponse buscarPorId(Long id) {
        Sebo sebo = seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(
                id, StatusVerificacaoSebo.VERIFICADO)
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado"));
        return seboMapper.toResponse(sebo);
    }

    @Transactional(readOnly = true)
    public SeboResponse buscarMeuSebo(Account account) {
        return seboMapper.toResponse(buscarEntidadePorAccount(account.getId()));
    }

    @Transactional(readOnly = true)
    public Sebo buscarEntidadePorAccount(Long accountId) {
        return seboRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado para esta conta"));
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> listarPendentes(Pageable pageable) {
        return seboRepository.findByStatusVerificacao(StatusVerificacaoSebo.PENDENTE, pageable)
                .map(seboMapper::toResponse);
    }

    @Transactional
    public MensagemResponse excluirConta(Account account) {
        anonimizacaoContaService.anonimizarSebo(account);
        return new MensagemResponse("Conta do sebo excluida com sucesso");
    }
}
