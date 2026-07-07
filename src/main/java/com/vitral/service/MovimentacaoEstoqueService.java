package com.vitral.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vitral.dto.EstornarVendaFisicaRequest;
import com.vitral.dto.MovimentacaoEstoqueResponse;
import com.vitral.dto.RegistrarVendaFisicaRequest;
import com.vitral.entity.Account;
import com.vitral.entity.MovimentacaoEstoque;
import com.vitral.entity.Produto;
import com.vitral.enumerations.TipoMovimentacaoEstoque;
import com.vitral.enumerations.TipoOperacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.MovimentacaoEstoqueRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository repository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final SeboService seboService;

    @Transactional
    public MovimentacaoEstoqueResponse registrarVendaFisica(Account operador, RegistrarVendaFisicaRequest request) {
        Produto produto = produtoDoSebo(request.produtoId(), operador);
        validarTipoOperacaoSebo(produto.getSebo());
        long reservado = pedidoRepository.sumQuantidadeReservadaEmPedidosPendentes(produto.getId());
        int disponivel = produto.getEstoque() - Math.toIntExact(reservado);
        if (request.quantidade() > disponivel) {
            throw new BusinessException("Estoque insuficiente: existem unidades reservadas em pedidos online",
                    HttpStatus.BAD_REQUEST);
        }
        MovimentacaoEstoque movimentacao = aplicarSaida(produto, operador, TipoMovimentacaoEstoque.VENDA_FISICA,
                request.quantidade(), request.valorUnitario(), request.observacao());
        seboService.registrarAtividade(produto.getSebo());
        return toResponse(movimentacao);
    }

    @Transactional
    public MovimentacaoEstoqueResponse estornarVendaFisica(
            Account operador, Long movimentacaoId, EstornarVendaFisicaRequest request) {
        MovimentacaoEstoque origem = repository.findById(movimentacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda fisica nao encontrada"));
        if (origem.getTipo() != TipoMovimentacaoEstoque.VENDA_FISICA) {
            throw new BusinessException("A movimentacao informada nao e uma venda fisica", HttpStatus.BAD_REQUEST);
        }
        if (!origem.getSebo().getAccount().getId().equals(operador.getId())) {
            throw new BusinessException("Venda fisica nao pertence ao sebo autenticado", HttpStatus.FORBIDDEN);
        }
        if (repository.existsByMovimentacaoOrigemIdAndTipo(movimentacaoId, TipoMovimentacaoEstoque.ESTORNO)) {
            throw new BusinessException("Venda fisica ja estornada", HttpStatus.CONFLICT);
        }
        Produto produto = origem.getProduto();
        int antes = produto.getEstoque();
        int depois = antes + origem.getQuantidade();
        produto.setEstoque(depois);
        produto.setAtivo(true);
        MovimentacaoEstoque estorno = salvar(produto, operador, TipoMovimentacaoEstoque.ESTORNO,
                origem.getQuantidade(), antes, depois, origem.getValorUnitario(), request.motivo(), origem);
        seboService.registrarAtividade(produto.getSebo());
        return toResponse(estorno);
    }

    @Transactional
    public MovimentacaoEstoque registrarAlteracao(Produto produto, Account operador,
            TipoMovimentacaoEstoque tipo, int quantidade, int antes, int depois,
            BigDecimal valorUnitario, String observacao) {
        return salvar(produto, operador, tipo, quantidade, antes, depois, valorUnitario, observacao, null);
    }

    @Transactional(readOnly = true)
    public Page<MovimentacaoEstoqueResponse> listar(Account account, Pageable pageable) {
        return repository.findBySeboAccountIdOrderByCreatedAtDesc(account.getId(), pageable).map(this::toResponse);
    }

    private MovimentacaoEstoque aplicarSaida(Produto produto, Account operador, TipoMovimentacaoEstoque tipo,
            int quantidade, BigDecimal valorUnitario, String observacao) {
        int antes = produto.getEstoque();
        int depois = antes - quantidade;
        produto.setEstoque(depois);
        produto.setAtivo(depois > 0);
        return salvar(produto, operador, tipo, quantidade, antes, depois, valorUnitario, observacao, null);
    }

    private MovimentacaoEstoque salvar(Produto produto, Account operador, TipoMovimentacaoEstoque tipo,
            int quantidade, int antes, int depois, BigDecimal valorUnitario, String observacao,
            MovimentacaoEstoque origem) {
        BigDecimal total = valorUnitario == null ? null : valorUnitario.multiply(BigDecimal.valueOf(quantidade));
        return repository.save(MovimentacaoEstoque.builder()
                .produto(produto).sebo(produto.getSebo()).operador(operador).movimentacaoOrigem(origem)
                .tipo(tipo).quantidade(quantidade).estoqueAntes(antes).estoqueDepois(depois)
                .valorUnitario(valorUnitario).valorTotal(total).observacao(observacao).build());
    }

    private Produto produtoDoSebo(Long produtoId, Account operador) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
        if (!produto.getSebo().getAccount().getId().equals(operador.getId())) {
            throw new BusinessException("Produto nao pertence ao sebo autenticado", HttpStatus.FORBIDDEN);
        }
        return produto;
    }

    private void validarTipoOperacaoSebo(com.vitral.entity.Sebo sebo) {
        if (sebo.getTipoOperacao() == TipoOperacaoSebo.VIRTUAL) {
            throw new BusinessException("Sebo virtual nao permite venda fisica", HttpStatus.BAD_REQUEST);
        }
    }

    private MovimentacaoEstoqueResponse toResponse(MovimentacaoEstoque m) {
        return new MovimentacaoEstoqueResponse(m.getId(), m.getProduto().getId(), m.getSebo().getId(),
                m.getOperador().getId(), m.getMovimentacaoOrigem() == null ? null : m.getMovimentacaoOrigem().getId(),
                m.getTipo(), m.getQuantidade(), m.getEstoqueAntes(), m.getEstoqueDepois(), m.getValorUnitario(),
                m.getValorTotal(), m.getObservacao(), m.getCreatedAt());
    }
}
