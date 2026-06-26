package com.vitral.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.CestaItemResponse;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.entity.CestaItem;
import com.vitral.entity.Produto;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.CestaItemRepository;
import com.vitral.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CestaService {

    private final CestaItemRepository cestaItemRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public MensagemResponse adicionarItem(Account account, Long produtoId, int quantidade) {
        if (cestaItemRepository.existsByAccountIdAndProdutoId(account.getId(), produtoId)) {
            throw new ConflictException("Produto ja esta na cesta");
        }
        Produto produto = produtoRepository.findByIdAndAtivoTrue(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        if (produto.getEstoque() < 1) {
            throw new BusinessException("Produto sem estoque disponivel", HttpStatus.BAD_REQUEST);
        }
        if (quantidade > produto.getEstoque()) {
            throw new BusinessException("Quantidade solicitada maior que o estoque disponivel", HttpStatus.BAD_REQUEST);
        }
        CestaItem item = CestaItem.builder()
                .account(account)
                .produto(produto)
                .quantidade(quantidade)
                .build();
        cestaItemRepository.save(item);
        return new MensagemResponse("Produto adicionado a cesta");
    }

    @Transactional
    public MensagemResponse atualizarQuantidade(Account account, Long produtoId, int quantidade) {
        if (quantidade < 1) {
            throw new BusinessException("Quantidade deve ser maior que zero", HttpStatus.BAD_REQUEST);
        }
        CestaItem item = cestaItemRepository
                .findByAccountIdAndProdutoId(account.getId(), produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado na cesta"));
        if (quantidade > item.getProduto().getEstoque()) {
            throw new BusinessException("Quantidade solicitada excede o estoque disponivel", HttpStatus.BAD_REQUEST);
        }
        item.setQuantidade(quantidade);
        return new MensagemResponse("Quantidade atualizada");
    }

    @Transactional(readOnly = true)
    public List<CestaItemResponse> listarCesta(Account account) {
        return cestaItemRepository.findByAccountIdComProdutoESebo(account.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MensagemResponse removerItem(Account account, Long produtoId) {
        CestaItem item = cestaItemRepository
                .findByAccountIdAndProdutoId(account.getId(), produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Item nao encontrado na cesta"));
        cestaItemRepository.delete(item);
        return new MensagemResponse("Produto removido da cesta");
    }

    private CestaItemResponse toResponse(CestaItem item) {
        Produto p = item.getProduto();
        BigDecimal subtotal = p.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new CestaItemResponse(
                item.getId(),
                p.getId(),
                p.getTitulo(),
                p.getAutor(),
                p.getPreco(),
                item.getQuantidade(),
                subtotal,
                p.getEstoque(),
                p.getCondicao(),
                p.getFotoUrl());
    }
}
