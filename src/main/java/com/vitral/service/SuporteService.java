package com.vitral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.SuporteRequest;
import com.vitral.entity.Account;
import com.vitral.entity.TicketSuporte;
import com.vitral.repository.TicketSuporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuporteService {

    private final TicketSuporteRepository ticketSuporteRepository;

    @Transactional
    public void enviar(Account account, SuporteRequest request) {
        TicketSuporte ticket = TicketSuporte.builder()
                .account(account)
                .assunto(request.assunto())
                .mensagem(request.mensagem())
                .build();
        ticketSuporteRepository.save(ticket);
    }
}
