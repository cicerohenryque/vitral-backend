package com.vitral.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.SeboResponse;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;
import com.vitral.specification.ProdutoSpecification;
import com.vitral.specification.SeboSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscaService {

    private final SeboRepository seboRepository;
    private final ProdutoRepository produtoRepository;
    private final SeboMapper seboMapper;
    private final ProdutoMapper produtoMapper;

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, String cidade, String uf, Pageable pageable) {
        Specification<Sebo> spec = Specification.where(null);

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(SeboSpecification.nomeContem(termo));
        }
        if (cidade != null && !cidade.isBlank()) {
            spec = spec.and(SeboSpecification.daCidade(cidade));
        }
        if (uf != null && !uf.isBlank()) {
            spec = spec.and(SeboSpecification.doEstado(uf));
        }

        return seboRepository.findAll(spec, pageable).map(seboMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> buscarProdutos(String termo, Long seboId, CondicaoProduto condicao,
            BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        Specification<Produto> spec = ProdutoSpecification.ativo();

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(ProdutoSpecification.tituloContem(termo));
        }
        if (seboId != null) {
            spec = spec.and(ProdutoSpecification.doSebo(seboId));
        }
        if (condicao != null) {
            spec = spec.and(ProdutoSpecification.comCondicao(condicao));
        }
        if (precoMin != null) {
            spec = spec.and(ProdutoSpecification.precoMinimo(precoMin));
        }
        if (precoMax != null) {
            spec = spec.and(ProdutoSpecification.precoMaximo(precoMax));
        }

        return produtoRepository.findAll(spec, pageable).map(produtoMapper::toResponse);
    }
}
