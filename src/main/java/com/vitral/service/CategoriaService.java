package com.vitral.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.CategoriaRequest;
import com.vitral.dto.CategoriaResponse;
import com.vitral.entity.Categoria;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.CategoriaMapper;
import com.vitral.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(categoriaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        boolean slugExistente = categoriaRepository.findAll()
                .stream()
                .anyMatch(c -> c.getSlug().equalsIgnoreCase(request.slug()));
        if (slugExistente) {
            throw new BusinessException("Slug ja utilizado por outra categoria", HttpStatus.CONFLICT);
        }
        Categoria categoria = Categoria.builder()
                .nome(request.nome())
                .slug(request.slug())
                .descricao(request.descricao())
                .build();
        return categoriaMapper.toResponse(categoriaRepository.save(categoria));
    }
}
