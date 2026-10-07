package com.vitral.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.FavoritoResponse;
import com.vitral.dto.MensagemResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Favorito;
import com.vitral.entity.Produto;
import com.vitral.exception.ConflictException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.FavoritoRepository;
import com.vitral.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;
import com.vitral.enumerations.TipoEventoRecomendacao;

@Service
@RequiredArgsConstructor
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final ProdutoRepository produtoRepository;
    private final PrecoService precoService;
    private final RecomendacaoEventoService recomendacaoEventoService;

    @Transactional
    public MensagemResponse favoritar(Account account, Long produtoId) {
        if (favoritoRepository.existsByAccountIdAndProdutoId(account.getId(), produtoId)) {
            throw new ConflictException("Este produto já está nos seus favoritos.");
        }
        Produto produto = produtoRepository.findByIdAndAtivoTrue(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("O produto informado não foi encontrado."));
        Favorito favorito = Favorito.builder()
                .account(account)
                .produto(produto)
                .build();
        favoritoRepository.save(favorito);
        recomendacaoEventoService.registrarProduto(account, TipoEventoRecomendacao.FAVORITO_ADICIONADO, produto);
        return new MensagemResponse("Produto adicionado aos favoritos com sucesso.");
    }

    @Transactional(readOnly = true)
    public List<FavoritoResponse> listarFavoritos(Account account) {
        List<Favorito> favoritos = favoritoRepository.findByAccountId(account.getId());
        List<Long> ids = favoritos.stream().map(f -> f.getProduto().getId()).toList();
        Map<Long, BigDecimal> promocionais = precoService.precosPromocionaisVigentes(ids);
        return favoritos.stream()
                .map(f -> new FavoritoResponse(
                        f.getId(),
                        f.getProduto().getId(),
                        f.getProduto().getTitulo(),
                        f.getProduto().getAutor(),
                        f.getProduto().getPreco(),
                        promocionais.get(f.getProduto().getId()),
                        f.getProduto().getCondicao(),
                        f.getProduto().getFotoUrl()))
                .toList();
    }

    @Transactional
    public MensagemResponse removerFavorito(Account account, Long produtoId) {
        Favorito favorito = favoritoRepository
                .findByAccountIdAndProdutoId(account.getId(), produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Este produto não foi encontrado nos seus favoritos."));
        favoritoRepository.delete(favorito);
        recomendacaoEventoService.registrarProduto(account, TipoEventoRecomendacao.FAVORITO_REMOVIDO,
                favorito.getProduto());
        return new MensagemResponse("Produto removido dos favoritos com sucesso.");
    }
}
