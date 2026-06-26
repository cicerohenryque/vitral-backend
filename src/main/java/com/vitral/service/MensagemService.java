package com.vitral.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.MensagemChatRequest;
import com.vitral.dto.MensagemChatResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Mensagem;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.MensagemMapper;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.MensagemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final AccountRepository accountRepository;
    private final MensagemMapper mensagemMapper;

    @Transactional
    public MensagemChatResponse enviar(Account remetente, MensagemChatRequest request) {
        if (remetente.getId().equals(request.destinatarioId())) {
            throw new BusinessException("Nao e possivel enviar mensagem para si mesmo", HttpStatus.BAD_REQUEST);
        }
        Account destinatario = accountRepository.findById(request.destinatarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Destinatario nao encontrado"));
        Mensagem mensagem = Mensagem.builder()
                .remetente(remetente)
                .destinatario(destinatario)
                .conteudo(request.conteudo())
                .build();
        return mensagemMapper.toResponse(mensagemRepository.save(mensagem));
    }

    @Transactional(readOnly = true)
    public Page<MensagemChatResponse> listar(Account account, Pageable pageable) {
        return mensagemRepository.findByUsuario(account.getId(), pageable)
                .map(mensagemMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MensagemChatResponse> listarConversa(Account account, Long outroAccountId, Pageable pageable) {
        return mensagemRepository.findConversa(account.getId(), outroAccountId, pageable)
                .map(mensagemMapper::toResponse);
    }
}
