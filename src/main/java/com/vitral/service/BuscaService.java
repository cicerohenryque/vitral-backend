package com.vitral.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private final PrecoService precoService;

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, Pageable pageable) {
        Specification<Sebo> spec = SeboSpecification.ativo();

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(SeboSpecification.nomeContem(termo));
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

        Page<Produto> produtos = produtoRepository.findAll(spec, pageable);
        List<Long> ids = produtos.getContent().stream().map(Produto::getId).toList();
        Map<Long, BigDecimal> promocionais = precoService.precosPromocionaisVigentes(ids);
        return produtos.map(p -> produtoMapper.toResponse(p, promocionais.get(p.getId())));
    }
}
