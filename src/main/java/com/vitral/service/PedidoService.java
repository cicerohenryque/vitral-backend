package com.vitral.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.PedidoItemResponse;
import com.vitral.dto.PedidoResponse;
import com.vitral.entity.Account;
import com.vitral.entity.CestaItem;
import com.vitral.entity.Pedido;
import com.vitral.entity.PedidoItem;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.StatusPedido;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.CestaItemRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.SeboRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CestaItemRepository cestaItemRepository;
    private final SeboRepository seboRepository;

    @Transactional
    public PedidoResponse confirmarPedido(Account account) {
        List<CestaItem> itens = cestaItemRepository.findByAccountIdComProdutoESebo(account.getId());
        if (itens.isEmpty()) {
            throw new BusinessException("A cesta esta vazia", HttpStatus.BAD_REQUEST);
        }

        boolean possuiProdutoInativo = itens.stream()
                .anyMatch(i -> !Boolean.TRUE.equals(i.getProduto().getAtivo()));
        if (possuiProdutoInativo) {
            throw new BusinessException(
                    "A cesta contem produtos que nao estao mais disponiveis",
                    HttpStatus.BAD_REQUEST);
        }

        Long seboId = itens.getFirst().getProduto().getSebo().getId();
        boolean sebosDistintos = itens.stream()
                .anyMatch(i -> !i.getProduto().getSebo().getId().equals(seboId));
        if (sebosDistintos) {
            throw new BusinessException(
                    "Todos os produtos da cesta devem pertencer ao mesmo sebo para realizar um pedido",
                    HttpStatus.BAD_REQUEST);
        }

        Sebo sebo = seboRepository.findById(seboId)
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado"));

        BigDecimal total = itens.stream()
                .map(i -> i.getProduto().getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Pedido pedido = Pedido.builder()
                .account(account)
                .sebo(sebo)
                .status(StatusPedido.AGUARDANDO_CONFIRMACAO)
                .total(total)
                .build();

        List<PedidoItem> pedidoItens = itens.stream()
                .map(i -> PedidoItem.builder()
                        .pedido(pedido)
                        .produto(i.getProduto())
                        .tituloSnapshot(i.getProduto().getTitulo())
                        .precoSnapshot(i.getProduto().getPreco())
                        .quantidade(i.getQuantidade())
                        .build())
                .toList();

        pedido.getItens().addAll(pedidoItens);
        Pedido salvo = pedidoRepository.save(pedido);
        cestaItemRepository.deleteByAccountId(account.getId());
        return toResponse(salvo);
    }

    @Transactional
    public PedidoResponse confirmarCompra(Account account, Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));

        if (!pedido.getSebo().getAccount().getId().equals(account.getId())) {
            throw new BusinessException("Pedido nao pertence ao sebo da conta autenticada", HttpStatus.FORBIDDEN);
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_CONFIRMACAO) {
            throw new BusinessException("Apenas pedidos com status AGUARDANDO_CONFIRMACAO podem ser atualizados",
                    HttpStatus.BAD_REQUEST);
        }
        if (novoStatus != StatusPedido.CONFIRMADO && novoStatus != StatusPedido.CANCELADO) {
            throw new BusinessException("Status invalido. Use CONFIRMADO ou CANCELADO", HttpStatus.BAD_REQUEST);
        }

        pedido.setStatus(novoStatus);
        return toResponse(pedido);
    }

    @Transactional
    public PedidoResponse cancelarPedido(Account account, Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));

        if (!pedido.getAccount().getId().equals(account.getId())) {
            throw new BusinessException("Pedido nao pertence ao usuario autenticado", HttpStatus.FORBIDDEN);
        }
        if (pedido.getStatus() != StatusPedido.AGUARDANDO_CONFIRMACAO) {
            throw new BusinessException(
                    "Apenas pedidos com status AGUARDANDO_CONFIRMACAO podem ser cancelados",
                    HttpStatus.BAD_REQUEST);
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> historicoPedidosUsuario(Account account, Pageable pageable) {
        return pedidoRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> historicoVendasSebo(Account account, Pageable pageable) {
        Sebo sebo = seboRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sebo nao encontrado para esta conta"));
        return pedidoRepository
                .findBySeboIdOrderByCreatedAtDesc(sebo.getId(), pageable)
                .map(this::toResponse);
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoItemResponse> itensResponse = pedido.getItens().stream()
                .map(i -> new PedidoItemResponse(
                        i.getId(),
                        i.getProduto().getId(),
                        i.getTituloSnapshot(),
                        i.getPrecoSnapshot(),
                        i.getQuantidade()))
                .toList();
        return new PedidoResponse(
                pedido.getId(),
                pedido.getAccount().getId(),
                pedido.getSebo().getId(),
                pedido.getStatus(),
                pedido.getTotal(),
                pedido.getCreatedAt(),
                itensResponse);
    }
}
