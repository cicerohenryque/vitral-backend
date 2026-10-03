package com.vitral.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.SeboResponse;
import com.vitral.entity.Produto;
import com.vitral.entity.Account;
import com.vitral.entity.Categoria;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.BookGenre;
import com.vitral.service.CategoriaService;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.mapper.SeboMapper;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;
import com.vitral.specification.ProdutoSpecification;
import com.vitral.specification.SeboSpecification;
import com.vitral.util.GeoUtils;
import com.vitral.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BuscaService {

    private final SeboRepository seboRepository;
    private final ProdutoRepository produtoRepository;
    private final SeboMapper seboMapper;
    private final ProdutoMapper produtoMapper;
    private final PrecoService precoService;
    private final CategoriaService categoriaService;
    private final RecomendacaoEventoService recomendacaoEventoService;

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, Pageable pageable) {
        return buscarSebos(termo, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, String cidade, String uf, Pageable pageable) {
        return buscarSebos(termo, cidade, uf, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, String cidade, String uf, Double lat, Double lng,
            Pageable pageable) {
        return buscarSebos(termo, cidade, uf, lat, lng, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SeboResponse> buscarSebos(String termo, String cidade, String uf, Double lat, Double lng,
            Double raioKm, Pageable pageable) {
        validarLocalizacao(lat, lng, raioKm);
        Specification<Sebo> spec = SeboSpecification.ativo();

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(SeboSpecification.nomeContem(termo));
        }
        if (cidade != null && !cidade.isBlank()) {
            spec = spec.and(SeboSpecification.cidadeContem(cidade));
        }
        if (uf != null && !uf.isBlank()) {
            spec = spec.and(SeboSpecification.ufIgual(uf));
        }

        if (lat != null && lng != null) {
            if (raioKm != null) {
                spec = spec.and(retanguloDeBusca(lat, lng, raioKm));
            }
            return buscarSebosPorProximidade(spec, lat, lng, raioKm, pageable);
        }
        return seboRepository.findAll(spec, pageable).map(seboMapper::toPublicResponse);
    }

    private Page<SeboResponse> buscarSebosPorProximidade(Specification<Sebo> spec, double lat, double lng,
            Double raioKm, Pageable pageable) {
        List<Sebo> encontrados = seboRepository.findAll(spec);
        List<SeboResponse> ordenados = encontrados.stream()
                .map(sebo -> seboMapper.toPublicResponse(sebo, distanciaKm(sebo, lat, lng)))
                .filter(sebo -> raioKm == null || sebo.distanciaKm() != null && sebo.distanciaKm() <= raioKm)
                .sorted(Comparator.comparing(SeboResponse::distanciaKm, Comparator.nullsLast(Double::compareTo))
                        .thenComparing(SeboResponse::id, Comparator.nullsLast(Long::compareTo)))
                .toList();

        int inicio = (int) Math.min(pageable.getOffset(), ordenados.size());
        int fim = (int) Math.min(inicio + pageable.getPageSize(), ordenados.size());
        return new PageImpl<>(ordenados.subList(inicio, fim), pageable, ordenados.size());
    }

    private void validarLocalizacao(Double lat, Double lng, Double raioKm) {
        if ((lat == null) != (lng == null)) {
            throw new BusinessException("Informe latitude e longitude juntas para buscar por proximidade.", HttpStatus.BAD_REQUEST);
        }
        if (lat != null && (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < -90 || lat > 90 || lng < -180 || lng > 180)) {
            throw new BusinessException("As coordenadas informadas são inválidas.", HttpStatus.BAD_REQUEST);
        }
        if (raioKm != null && (lat == null || !Double.isFinite(raioKm) || raioKm <= 0 || raioKm > 500)) {
            throw new BusinessException("O raio deve ser maior que zero, ter no máximo 500 km e requer uma localização.",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private Specification<Sebo> retanguloDeBusca(double lat, double lng, double raioKm) {
        double latitudeDelta = raioKm / 111.0;
        double cosseno = Math.max(Math.cos(Math.toRadians(lat)), 0.01);
        double longitudeDelta = raioKm / (111.0 * cosseno);
        return SeboSpecification.dentroDoRetangulo(lat - latitudeDelta, lat + latitudeDelta,
                lng - longitudeDelta, lng + longitudeDelta);
    }

    private Double distanciaKm(Sebo sebo, double lat, double lng) {
        if (sebo.getLatitude() == null || sebo.getLongitude() == null) {
            return null;
        }
        return GeoUtils.haversineKm(lat, lng, sebo.getLatitude(), sebo.getLongitude());
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> buscarProdutos(String termo, Long seboId, CondicaoProduto condicao,
            BigDecimal precoMin, BigDecimal precoMax, Long categoriaId, BookGenre bookGenre, Pageable pageable) {
        return buscarProdutos(null, termo, seboId, condicao, precoMin, precoMax, categoriaId, bookGenre, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> buscarProdutos(Account account, String termo, Long seboId, CondicaoProduto condicao,
            BigDecimal precoMin, BigDecimal precoMax, Long categoriaId, BookGenre bookGenre, Pageable pageable) {
        Specification<Produto> spec = ProdutoSpecification.ativo();
        Categoria categoriaPesquisa = null;

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(ProdutoSpecification.tituloOuAutorContem(termo));
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
        if (categoriaId != null) {
            var categoria = categoriaService.buscarPermitida(categoriaId);
            categoriaPesquisa = categoria;
            if (bookGenre != null && !"livros".equals(categoria.getSlug())) {
                throw new com.vitral.exception.BusinessException(
                        "bookGenre somente pode ser informado para a categoria Livros", org.springframework.http.HttpStatus.BAD_REQUEST);
            }
            spec = spec.and(ProdutoSpecification.daCategoria(categoriaId));
        } else if (bookGenre != null) {
            throw new com.vitral.exception.BusinessException(
                    "categoriaId de Livros e obrigatorio ao filtrar por bookGenre", org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        if (bookGenre != null) spec = spec.and(ProdutoSpecification.comBookGenre(bookGenre));

        Page<Produto> produtos = produtoRepository.findAll(spec, ordenarPorPrecoEfetivo(pageable));
        Produto primeiroResultado = produtos.isEmpty() ? null : produtos.getContent().getFirst();
        Categoria categoriaSinal = categoriaPesquisa != null ? categoriaPesquisa
                : (primeiroResultado == null ? null : primeiroResultado.getCategoria());
        BookGenre generoSinal = bookGenre != null ? bookGenre
                : (primeiroResultado == null ? null : primeiroResultado.getBookGenre());
        BigDecimal precoSinal = primeiroResultado == null ? null : primeiroResultado.getPreco();
        recomendacaoEventoService.registrarPesquisa(account, termo, categoriaSinal, generoSinal,
                precoMin != null ? precoMin : precoSinal, precoMax != null ? precoMax : precoSinal);
        List<Long> ids = produtos.getContent().stream().map(Produto::getId).toList();
        Map<Long, BigDecimal> promocionais = precoService.precosPromocionaisVigentes(ids);
        return produtos.map(p -> produtoMapper.toResponse(p, promocionais.get(p.getId())));
    }

    private Pageable ordenarPorPrecoEfetivo(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getSort().getOrderFor("preco") == null) {
            return pageable;
        }

        List<Sort.Order> orders = new ArrayList<>();
        pageable.getSort().forEach(order -> orders.add(
                "preco".equals(order.getProperty()) ? order.withProperty("precoEfetivo") : order));
        if (orders.stream().noneMatch(order -> "id".equals(order.getProperty()))) {
            orders.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    public Page<ProdutoResponse> buscarProdutos(String termo, Long seboId, CondicaoProduto condicao,
            BigDecimal precoMin, BigDecimal precoMax, Pageable pageable) {
        return buscarProdutos(termo, seboId, condicao, precoMin, precoMax, null, null, pageable);
    }
}
