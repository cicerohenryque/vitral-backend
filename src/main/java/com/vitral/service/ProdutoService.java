package com.vitral.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.VendaFisicaRequest;
import com.vitral.dto.VendedorProdutoResponse;
import com.vitral.enumerations.StatusVerificacaoSebo;
import java.util.Comparator;
import org.springframework.data.jpa.domain.Specification;
import com.vitral.specification.ProdutoSpecification;
import com.vitral.entity.Account;
import com.vitral.entity.Categoria;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.ProdutoMapper;
import com.vitral.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoMapper produtoMapper;
    private final SeboService seboService;
    private final CategoriaService categoriaService;
    private final PrecoService precoService;
    private final com.vitral.repository.PedidoRepository pedidoRepository;
    private final CatalogoLivroService catalogoLivroService;

    @Transactional
    public ProdutoResponse criar(Account account, ProdutoRequest request) {
        Sebo sebo = seboService.buscarEntidadePorAccount(account.getId());
        Categoria categoria = obterCategoria(request.categoriaId());
        Produto produto = Produto.builder()
                .sebo(sebo)
                .categoria(categoria)
                .catalogoLivro(catalogoLivroService.obterOuCriar(request))
                .titulo(request.titulo())
                .autor(request.autor())
                .descricao(request.descricao())
                .preco(request.preco())
                .estoque(normalizarEstoque(request.estoque()))
                .condicao(request.condicao())
                .fotoUrl(request.fotoUrl())
                .ativo(normalizarEstoque(request.estoque()) > 0)
                .build();
        Produto salvo = produtoRepository.save(produto);
        return toResponse(salvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Account account, Long produtoId, ProdutoRequest request) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        produto.setCategoria(obterCategoria(request.categoriaId()));
        produto.setCatalogoLivro(catalogoLivroService.obterOuCriar(request));
        produto.setTitulo(request.titulo());
        produto.setAutor(request.autor());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(normalizarEstoque(request.estoque()));
        produto.setCondicao(request.condicao());
        produto.setFotoUrl(request.fotoUrl());
        produto.setAtivo(produto.getEstoque() > 0);
        return toResponse(produto);
    }

    @Transactional
    public void remover(Account account, Long produtoId) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        produto.setAtivo(Boolean.FALSE);
    }

    @Transactional
    public ProdutoResponse registrarVendaFisica(Account account, Long produtoId, VendaFisicaRequest request) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        long reservado = pedidoRepository.sumQuantidadeReservadaEmPedidosPendentes(produtoId);
        int disponivel = produto.getEstoque() - Math.toIntExact(reservado);
        if (request.quantidade() > disponivel) {
            throw new BusinessException("Estoque insuficiente: existem unidades reservadas em pedidos online",
                    HttpStatus.BAD_REQUEST);
        }
        int novoEstoque = produto.getEstoque() - request.quantidade();
        produto.setEstoque(novoEstoque);
        produto.setAtivo(novoEstoque > 0);
        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = produtoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        validarSeboVerificado(produto);
        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<VendedorProdutoResponse> listarVendedores(Long produtoId) {
        Produto referencia = produtoRepository.findByIdAndAtivoTrue(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        List<Produto> candidatos = referencia.getCatalogoLivro() == null
                ? List.of(referencia)
                : produtoRepository.vendedoresAtivosPorCatalogo(referencia.getCatalogoLivro().getId());
        return candidatos.stream()
                .map(produto -> {
                    BigDecimal promocional = precoService.precoPromocionalVigente(produto).orElse(null);
                    BigDecimal efetivo = promocional == null ? produto.getPreco() : promocional;
                    return new VendedorProdutoResponse(produto.getId(), produto.getSebo().getId(),
                            produto.getSebo().getAccount().getName(), produto.getPreco(), promocional,
                            efetivo, produto.getEstoque(), produto.getCondicao());
                })
                .sorted(Comparator.comparing(VendedorProdutoResponse::precoEfetivo))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorSebo(Long seboId, Pageable pageable) {
        return toResponsePage(produtoRepository.findAll(
                ProdutoSpecification.ativo().and(ProdutoSpecification.doSebo(seboId)), pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        categoriaService.buscarEntidade(categoriaId);
        Specification<Produto> spec = ProdutoSpecification.ativo()
                .and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        return toResponsePage(produtoRepository.findAll(spec, pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarLancamentos(Pageable pageable) {
        return toResponsePage(produtoRepository.findAll(ProdutoSpecification.ativo(), pageable));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarClassicos(Pageable pageable) {
        return toResponsePage(produtoRepository.findAll(ProdutoSpecification.ativo(), pageable));
    }

    private ProdutoResponse toResponse(Produto produto) {
        return produtoMapper.toResponse(produto, precoService.precoPromocionalVigente(produto).orElse(null));
    }

    private Page<ProdutoResponse> toResponsePage(Page<Produto> produtos) {
        List<Long> ids = produtos.getContent().stream().map(Produto::getId).toList();
        Map<Long, BigDecimal> promocionais = precoService.precosPromocionaisVigentes(ids);
        return produtos.map(p -> produtoMapper.toResponse(p, promocionais.get(p.getId())));
    }

    private Produto obterProdutoDoDono(Account account, Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        if (!produto.getSebo().getAccount().getId().equals(account.getId())) {
            throw new BusinessException("Produto nao pertence ao sebo da conta autenticada", HttpStatus.FORBIDDEN);
        }
        return produto;
    }

    private Categoria obterCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaService.buscarEntidade(categoriaId);
    }

    private int normalizarEstoque(Integer estoque) {
        return estoque == null ? 1 : estoque;
    }

    private void validarSeboVerificado(Produto produto) {
        if (produto.getSebo().getStatusVerificacao() != StatusVerificacaoSebo.VERIFICADO
                || !Boolean.TRUE.equals(produto.getSebo().getAccount().getAtivo())) {
            throw new ResourceNotFoundException("Produto nao encontrado");
        }
    }
}
