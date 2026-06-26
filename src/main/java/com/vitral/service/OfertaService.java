package com.vitral.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.OfertaRequest;
import com.vitral.dto.OfertaResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Oferta;
import com.vitral.entity.Produto;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.mapper.OfertaMapper;
import com.vitral.repository.OfertaRepository;
import com.vitral.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfertaService {

    private final OfertaRepository ofertaRepository;
    private final ProdutoRepository produtoRepository;
    private final OfertaMapper ofertaMapper;

    @Transactional(readOnly = true)
    public Page<OfertaResponse> listarAtivas(Pageable pageable) {
        return ofertaRepository.findByAtivaTrue(pageable).map(ofertaMapper::toResponse);
    }

    @Transactional
    public OfertaResponse criar(Account account, OfertaRequest request) {
        Produto produto = buscarProdutoDoDono(account, request.produtoId());
        if (request.precoPromocional().compareTo(produto.getPreco()) >= 0) {
            throw new BusinessException("Preco promocional deve ser menor que o preco original", HttpStatus.BAD_REQUEST);
        }
        Oferta oferta = Oferta.builder()
                .produto(produto)
                .precoOriginal(produto.getPreco())
                .precoPromocional(request.precoPromocional())
                .descricao(request.descricao())
                .inicioEm(request.inicioEm())
                .fimEm(request.fimEm())
                .ativa(request.ativa() != null ? request.ativa() : Boolean.TRUE)
                .build();
        return ofertaMapper.toResponse(ofertaRepository.save(oferta));
    }

    @Transactional
    public OfertaResponse atualizar(Account account, Long ofertaId, OfertaRequest request) {
        Oferta oferta = buscarOfertaDoDono(account, ofertaId);
        Produto produto = oferta.getProduto();
        if (request.precoPromocional().compareTo(produto.getPreco()) >= 0) {
            throw new BusinessException("Preco promocional deve ser menor que o preco original", HttpStatus.BAD_REQUEST);
        }
        oferta.setPrecoOriginal(produto.getPreco());
        oferta.setPrecoPromocional(request.precoPromocional());
        oferta.setDescricao(request.descricao());
        oferta.setInicioEm(request.inicioEm());
        oferta.setFimEm(request.fimEm());
        if (request.ativa() != null) {
            oferta.setAtiva(request.ativa());
        }
        return ofertaMapper.toResponse(oferta);
    }

    @Transactional
    public void remover(Account account, Long ofertaId) {
        Oferta oferta = buscarOfertaDoDono(account, ofertaId);
        ofertaRepository.delete(oferta);
    }

    private Produto buscarProdutoDoDono(Account account, Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        if (!produto.getSebo().getAccount().getId().equals(account.getId())) {
            throw new BusinessException("Produto nao pertence ao sebo da conta autenticada", HttpStatus.FORBIDDEN);
        }
        return produto;
    }

    private Oferta buscarOfertaDoDono(Account account, Long ofertaId) {
        return ofertaRepository.findByIdAndProdutoSeboAccountId(ofertaId, account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Oferta nao encontrada"));
    }
}
