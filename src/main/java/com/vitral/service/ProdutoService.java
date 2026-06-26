package com.vitral.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
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

    @Transactional
    public ProdutoResponse criar(Account account, ProdutoRequest request) {
        Sebo sebo = seboService.buscarEntidadePorAccount(account.getId());
        Categoria categoria = request.categoriaId() != null
                ? categoriaService.buscarEntidade(request.categoriaId()) : null;
        Produto produto = Produto.builder()
                .sebo(sebo)
                .categoria(categoria)
                .titulo(request.titulo())
                .autor(request.autor())
                .descricao(request.descricao())
                .preco(request.preco())
                .estoque(request.estoque() != null ? request.estoque() : 1)
                .condicao(request.condicao())
                .fotoUrl(request.fotoUrl())
                .ativo(Boolean.TRUE)
                .build();
        return produtoMapper.toResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Account account, Long produtoId, ProdutoRequest request) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        Categoria categoria = request.categoriaId() != null
                ? categoriaService.buscarEntidade(request.categoriaId()) : null;
        produto.setCategoria(categoria);
        produto.setTitulo(request.titulo());
        produto.setAutor(request.autor());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(request.estoque() != null ? request.estoque() : produto.getEstoque());
        produto.setCondicao(request.condicao());
        produto.setFotoUrl(request.fotoUrl());
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    public void remover(Account account, Long produtoId) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        produto.setAtivo(Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return produtoMapper.toResponse(produtoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado")));
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorSebo(Long seboId, Pageable pageable) {
        return produtoRepository.findBySeboIdAndAtivoTrue(seboId, pageable)
                .map(produtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarLancamentos(Pageable pageable) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return produtoRepository.findByAtivoTrue(ordenado).map(produtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarClassicos(Pageable pageable) {
        Pageable ordenado = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt"));
        return produtoRepository.findByAtivoTrue(ordenado).map(produtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        return produtoRepository.findByCategoriaIdAndAtivoTrue(categoriaId, pageable)
                .map(produtoMapper::toResponse);
    }

    private Produto obterProdutoDoDono(Account account, Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        if (!produto.getSebo().getAccount().getId().equals(account.getId())) {
            throw new BusinessException("Produto nao pertence ao sebo da conta autenticada", HttpStatus.FORBIDDEN);
        }
        return produto;
    }
}
