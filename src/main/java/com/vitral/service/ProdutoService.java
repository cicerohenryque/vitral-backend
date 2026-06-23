package com.vitral.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Categoria categoria = obterCategoria(request.categoriaId());
        Produto produto = Produto.builder()
                .sebo(sebo)
                .categoria(categoria)
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
        return produtoMapper.toResponse(salvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Account account, Long produtoId, ProdutoRequest request) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        produto.setCategoria(obterCategoria(request.categoriaId()));
        produto.setTitulo(request.titulo());
        produto.setAutor(request.autor());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setEstoque(normalizarEstoque(request.estoque()));
        produto.setCondicao(request.condicao());
        produto.setFotoUrl(request.fotoUrl());
        produto.setAtivo(produto.getEstoque() > 0);
        return produtoMapper.toResponse(produto);
    }

    @Transactional
    public void remover(Account account, Long produtoId) {
        Produto produto = obterProdutoDoDono(account, produtoId);
        produto.setAtivo(Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        Produto produto = produtoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        return produtoMapper.toResponse(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorSebo(Long seboId, Pageable pageable) {
        return produtoRepository.findBySeboIdAndAtivoTrue(seboId, pageable)
                .map(produtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarPorCategoria(Long categoriaId, Pageable pageable) {
        categoriaService.buscarEntidade(categoriaId);
        return produtoRepository.findByCategoriaIdAndAtivoTrue(categoriaId, pageable)
                .map(produtoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponse> listarLancamentos(Pageable pageable) {
        return produtoRepository.findByAtivoTrue(pageable)
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

    private Categoria obterCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaService.buscarEntidade(categoriaId);
    }

    private int normalizarEstoque(Integer estoque) {
        return estoque == null ? 1 : estoque;
    }
}
