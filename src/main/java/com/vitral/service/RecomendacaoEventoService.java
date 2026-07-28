package com.vitral.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vitral.config.RecomendacaoProperties;
import com.vitral.entity.Account;
import com.vitral.entity.Categoria;
import com.vitral.entity.Pedido;
import com.vitral.entity.Produto;
import com.vitral.entity.RecomendacaoEvento;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.BookGenre;
import com.vitral.enumerations.FaixaPrecoRecomendacao;
import com.vitral.enumerations.TipoEventoRecomendacao;
import com.vitral.repository.RecomendacaoEventoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecomendacaoEventoService {
    private static final Logger log = LoggerFactory.getLogger(RecomendacaoEventoService.class);

    private final RecomendacaoEventoRepository repository;
    private final RecomendacaoEventoPersistenceService persistence;
    private final NormalizadorPesquisaService normalizador;
    private final RecomendacaoProperties properties;

    public void registrarProduto(Account account, TipoEventoRecomendacao tipo, Produto produto) {
        try {
            registrarProdutoInterno(account, tipo, produto, null);
        } catch (RuntimeException exception) {
            log.warn("Falha ao registrar evento de recomendacao do tipo {}", tipo);
        }
    }

    public void registrarCompra(Account account, Pedido pedido, Produto produto) {
        try {
            if (!rastreavel(account) || repository.existsByPedidoIdAndProdutoIdAndTipo(
                    pedido.getId(), produto.getId(), TipoEventoRecomendacao.COMPRA_CONCLUIDA)) return;
            registrarProdutoInterno(account, TipoEventoRecomendacao.COMPRA_CONCLUIDA, produto, pedido);
        } catch (RuntimeException exception) {
            log.warn("Falha ao registrar evento de compra para recomendacao");
        }
    }

    public void registrarPesquisa(Account account, String termo, Categoria categoria, BookGenre genero,
            BigDecimal precoMin, BigDecimal precoMax) {
        try {
            registrarPesquisaInterno(account, termo, categoria, genero, precoMin, precoMax);
        } catch (RuntimeException exception) {
            log.warn("Falha ao registrar evento de pesquisa para recomendacao");
        }
    }

    private void registrarPesquisaInterno(Account account, String termo, Categoria categoria, BookGenre genero,
            BigDecimal precoMin, BigDecimal precoMax) {
        if (!rastreavel(account)) return;
        String normalizado = normalizador.normalizar(termo);
        if (normalizado == null && categoria == null && genero == null && precoMin == null && precoMax == null) return;
        OffsetDateTime agora = OffsetDateTime.now();
        if (normalizado != null && (repository.existsByAccountIdAndTipoAndTermoPesquisaAndCreatedAtAfter(
                account.getId(), TipoEventoRecomendacao.PESQUISA, normalizado,
                agora.minusMinutes(properties.janelaVisualizacaoMinutos()))
                || repository.countByAccountIdAndTipoAndTermoPesquisaAndCreatedAtAfter(account.getId(),
                        TipoEventoRecomendacao.PESQUISA, normalizado, inicioDoDia(agora))
                        >= properties.eventosRepetidosDiaMaximo())) return;
        BigDecimal referencia = precoMin != null && precoMax != null
                ? precoMin.add(precoMax).divide(BigDecimal.valueOf(2)) : (precoMin != null ? precoMin : precoMax);
        salvarSeguro(RecomendacaoEvento.builder().account(account).tipo(TipoEventoRecomendacao.PESQUISA)
                .categoria(categoria).tipoProduto(categoria == null ? null : categoria.getSlug())
                .genero(genero == null ? null : genero.name())
                .faixaPreco(referencia == null ? null : FaixaPrecoRecomendacao.de(referencia).name())
                .termoPesquisa(normalizado).build());
    }

    private void registrarProdutoInterno(Account account, TipoEventoRecomendacao tipo, Produto produto, Pedido pedido) {
        if (!rastreavel(account) || produto == null) return;
        if (tipo == TipoEventoRecomendacao.VISUALIZACAO && repository
                .existsByAccountIdAndProdutoIdAndTipoAndCreatedAtAfter(account.getId(), produto.getId(), tipo,
                        OffsetDateTime.now().minusMinutes(properties.janelaVisualizacaoMinutos()))) return;
        if (tipo == TipoEventoRecomendacao.VISUALIZACAO && repository
                .countByAccountIdAndProdutoIdAndTipoAndCreatedAtAfter(account.getId(), produto.getId(), tipo,
                        inicioDoDia(OffsetDateTime.now())) >= properties.eventosRepetidosDiaMaximo()) return;
        salvarSeguro(RecomendacaoEvento.builder().account(account).tipo(tipo).produto(produto)
                .categoria(produto.getCategoria()).sebo(produto.getSebo()).pedido(pedido)
                .tipoProduto(produto.getCategoria() == null ? null : produto.getCategoria().getSlug())
                .genero(produto.getBookGenre() == null ? null : produto.getBookGenre().name())
                .autorArtista(normalizarTexto(produto.getAutor()))
                .faixaPreco(FaixaPrecoRecomendacao.de(produto.getPreco()).name()).build());
    }

    private boolean rastreavel(Account account) {
        return account != null && account.getId() != null && account.getType() == AccountType.USUARIO;
    }

    private void salvarSeguro(RecomendacaoEvento evento) {
        try {
            persistence.salvar(evento);
        } catch (RuntimeException exception) {
            log.warn("Falha ao registrar evento de recomendacao do tipo {}", evento.getTipo());
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toLowerCase();
    }

    private OffsetDateTime inicioDoDia(OffsetDateTime agora) {
        return agora.toLocalDate().atStartOfDay().atOffset(agora.getOffset());
    }
}
