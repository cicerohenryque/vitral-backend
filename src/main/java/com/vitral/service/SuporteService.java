package com.vitral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.SuporteRequest;
import com.vitral.dto.SuporteResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Suporte;
import com.vitral.repository.SuporteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuporteService {

    private final SuporteRepository suporteRepository;

    @Transactional
    public SuporteResponse enviar(Account remetente, SuporteRequest request) {
        Suporte suporte = Suporte.builder()
                .assunto(request.assunto())
                .mensagem(request.mensagem())
                .remetente(remetente)
                .build();
        return toResponse(suporteRepository.save(suporte));
    }

    private SuporteResponse toResponse(Suporte suporte) {
        return new SuporteResponse(
                suporte.getId(),
                suporte.getAssunto(),
                suporte.getMensagem(),
                suporte.getStatus().name(),
                suporte.getCreatedAt());
    }
}
