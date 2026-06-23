package com.vitral.service;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.CategoriaRequest;
import com.vitral.dto.CategoriaResponse;
import com.vitral.entity.Categoria;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String slug = gerarSlug(request.nome());
        if (categoriaRepository.existsBySlug(slug)) {
            throw new ConflictException("Categoria ja cadastrada");
        }
        Categoria categoria = Categoria.builder()
                .nome(request.nome().trim())
                .slug(slug)
                .descricao(request.descricao())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponse> listar(Pageable pageable) {
        return categoriaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria nao encontrada"));
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getSlug(),
                categoria.getDescricao());
    }

    private String gerarSlug(String nome) {
        String semAcentos = Normalizer.normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
