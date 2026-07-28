package com.vitral.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.entity.RecomendacaoEvento;
import com.vitral.repository.RecomendacaoEventoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecomendacaoEventoPersistenceService {
    private final RecomendacaoEventoRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvar(RecomendacaoEvento evento) {
        repository.save(evento);
    }
}
