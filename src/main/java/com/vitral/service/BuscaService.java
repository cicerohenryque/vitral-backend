package com.vitral.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.SeboResponse;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.TipoOperacaoSebo;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;
import com.vitral.specification.ProdutoSpecification;
import com.vitral.specification.SeboSpecification;
import com.vitral.util.GeoUtils;

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
        return buscarSebos(termo, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, TipoOperacaoSebo tipoOperacao,
            String cidade, String uf, Pageable pageable) {
        return buscarSebos(termo, tipoOperacao, cidade, uf, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, TipoOperacaoSebo tipoOperacao,
            String cidade, String uf, Double lat, Double lng, Pageable pageable) {
        Specification<Sebo> spec = SeboSpecification.ativo();

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(SeboSpecification.nomeContem(termo));
        }
        if (tipoOperacao != null) {
            spec = spec.and(SeboSpecification.tipoOperacao(tipoOperacao));
        }
        if (cidade != null && !cidade.isBlank()) {
            spec = spec.and(SeboSpecification.cidadeContem(cidade));
        }
        if (uf != null && !uf.isBlank()) {
            spec = spec.and(SeboSpecification.ufIgual(uf));
        }

        if (lat != null && lng != null) {
            return buscarSebosPorProximidade(spec, lat, lng, pageable);
        }
        return seboRepository.findAll(spec, pageable).map(seboMapper::toResponse);
    }

    private Page<SeboResponse> buscarSebosPorProximidade(Specification<Sebo> spec, double lat, double lng,
            Pageable pageable) {
        List<Sebo> encontrados = seboRepository.findAll(spec);
        List<SeboResponse> ordenados = encontrados.stream()
                .map(sebo -> seboMapper.toResponse(sebo, distanciaKm(sebo, lat, lng)))
                .sorted(Comparator.comparing(SeboResponse::distanciaKm, Comparator.nullsLast(Double::compareTo)))
                .toList();

        int inicio = (int) Math.min(pageable.getOffset(), ordenados.size());
        int fim = (int) Math.min(inicio + pageable.getPageSize(), ordenados.size());
        return new PageImpl<>(ordenados.subList(inicio, fim), pageable, ordenados.size());
    }

    private Double distanciaKm(Sebo sebo, double lat, double lng) {
        if (sebo.getLatitude() == null || sebo.getLongitude() == null) {
            return null;
        }
        return GeoUtils.haversineKm(lat, lng, sebo.getLatitude(), sebo.getLongitude());
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
