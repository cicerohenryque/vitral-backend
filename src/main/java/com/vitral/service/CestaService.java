package com.vitral.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.CestaItemResponse;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.entity.CestaItem;
import com.vitral.entity.Produto;
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
    public MensagemResponse adicionarItem(Account account, Long produtoId) {
        if (cestaItemRepository.existsByAccountIdAndProdutoId(account.getId(), produtoId)) {
            throw new ConflictException("Produto ja esta na cesta");
        }
        Produto produto = produtoRepository.findByIdAndAtivoTrue(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        CestaItem item = CestaItem.builder()
                .account(account)
                .produto(produto)
                .build();
        cestaItemRepository.save(item);
        return new MensagemResponse("Produto adicionado a cesta");
    }

    @Transactional(readOnly = true)
    public List<CestaItemResponse> listarCesta(Account account) {
        return cestaItemRepository.findByAccountId(account.getId()).stream()
                .map(i -> new CestaItemResponse(
                        i.getId(),
                        i.getProduto().getId(),
                        i.getProduto().getTitulo(),
                        i.getProduto().getAutor(),
                        i.getProduto().getPreco(),
                        i.getProduto().getCondicao(),
                        i.getProduto().getFotoUrl()))
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
}
