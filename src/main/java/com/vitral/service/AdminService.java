package com.vitral.service;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.AdminAccountResponse;
import com.vitral.dto.AdminAuditResponse;
import com.vitral.dto.AdminDashboardResponse;
import com.vitral.dto.AdminPedidoResponse;
import com.vitral.dto.AdminProdutoResponse;
import com.vitral.dto.AdminSeboResponse;
import com.vitral.entity.Account;
import com.vitral.entity.AdminAudit;
import com.vitral.entity.Pedido;
import com.vitral.entity.Produto;
import com.vitral.entity.Sebo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusPedido;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.AccountRepository;
import com.vitral.repository.AdminAuditRepository;
import com.vitral.repository.PedidoRepository;
import com.vitral.repository.ProdutoRepository;
import com.vitral.repository.SeboRepository;
import com.vitral.specification.AccountSpecification;
import com.vitral.specification.AdminAuditSpecification;
import com.vitral.specification.PedidoSpecification;
import com.vitral.specification.ProdutoSpecification;
import com.vitral.specification.SeboSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AccountRepository accountRepository;
    private final ProdutoRepository produtoRepository;
    private final SeboRepository seboRepository;
    private final PedidoRepository pedidoRepository;
    private final AdminAuditRepository adminAuditRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        Map<String, Long> pedidosPorStatus = new LinkedHashMap<>();
        pedidoRepository.countByStatusGrouped().forEach(row -> pedidosPorStatus.put(row[0].toString(), (Long) row[1]));
        return new AdminDashboardResponse(accountRepository.countByType(AccountType.USUARIO),
                accountRepository.countByTypeAndAtivoTrue(AccountType.USUARIO),
                seboRepository.count(), produtoRepository.count(), produtoRepository.countByAtivoTrue(),
                pedidoRepository.count(), pedidosPorStatus);
    }

    @Transactional(readOnly = true)
    public Page<AdminAccountResponse> listarContas(String termo, AccountType tipo, Boolean ativo, Pageable pageable) {
        Specification<Account> spec = AccountSpecification.todos()
                .and(AccountSpecification.termo(termo)).and(AccountSpecification.tipo(tipo))
                .and(AccountSpecification.ativo(ativo));
        return accountRepository.findAll(spec, pageable).map(this::contaResponse);
    }

    @Transactional(readOnly = true)
    public AdminAccountResponse buscarConta(Long id) {
        return contaResponse(accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A conta informada não foi encontrada.")));
    }

    @Transactional
    public AdminAccountResponse alterarStatusConta(Account admin, Long id, boolean ativo) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("A conta informada não foi encontrada."));
        if (admin.getId().equals(id)) {
            throw new BusinessException("Você não pode desativar a própria conta administrativa.", HttpStatus.BAD_REQUEST);
        }
        if (account.getType() == AccountType.ADMIN) {
            throw new BusinessException("Contas administrativas não podem ser alteradas por esta operação.", HttpStatus.BAD_REQUEST);
        }
        if (Boolean.TRUE.equals(account.getAtivo()) == ativo) return contaResponse(account);
        account.setAtivo(ativo);
        account.setAuthVersion(account.getAuthVersion() + 1);
        auditar(admin, ativo ? "REATIVOU" : "DESATIVOU", "CONTA", id,
                ativo ? "Conta reativada." : "Conta desativada.");
        return contaResponse(account);
    }

    @Transactional(readOnly = true)
    public Page<AdminProdutoResponse> listarProdutos(String termo, Long seboId, Boolean ativo, Pageable pageable) {
        Specification<Produto> spec = ProdutoSpecification.ativoIgual(ativo).and(ProdutoSpecification.doSebo(seboId));
        if (termo != null && !termo.isBlank()) spec = spec.and(ProdutoSpecification.tituloContem(termo.trim()));
        return produtoRepository.findAll(spec, pageable).map(this::produtoResponse);
    }

    @Transactional(readOnly = true)
    public AdminProdutoResponse buscarProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("O produto informado não foi encontrado."));
        return produtoResponse(produto);
    }

    @Transactional
    public AdminProdutoResponse alterarStatusProduto(Account admin, Long id, boolean ativo) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("O produto informado não foi encontrado."));
        if (Boolean.TRUE.equals(produto.getAtivo()) == ativo) return produtoResponse(produto);
        if (ativo && (produto.getEstoque() == null || produto.getEstoque() <= 0)) {
            throw new BusinessException("Não é possível ativar um produto sem estoque.", HttpStatus.BAD_REQUEST);
        }
        produto.setAtivo(ativo);
        auditar(admin, ativo ? "REATIVOU" : "DESATIVOU", "PRODUTO", id,
                (ativo ? "Produto reativado: " : "Produto desativado: ") + produto.getTitulo());
        return produtoResponse(produto);
    }

    @Transactional(readOnly = true)
    public Page<AdminSeboResponse> listarSebos(String termo, StatusVerificacaoSebo status, Pageable pageable) {
        Specification<Sebo> spec = SeboSpecification.todos().and(SeboSpecification.statusIgual(status));
        if (termo != null && !termo.isBlank()) spec = spec.and(SeboSpecification.nomeContem(termo.trim()));
        Page<Sebo> sebos = seboRepository.findAll(spec, pageable);
        Map<Long, long[]> totais = contagensProdutos(sebos.getContent());
        return sebos.map(sebo -> seboResponse(sebo, totais.getOrDefault(sebo.getId(), new long[2])));
    }

    @Transactional(readOnly = true)
    public Page<AdminPedidoResponse> listarPedidos(Long id, StatusPedido status, OffsetDateTime inicio,
            OffsetDateTime fim, Pageable pageable) {
        Page<Pedido> pedidos = pedidoRepository.findAll(PedidoSpecification.filtros(id, status, inicio, fim), pageable);
        List<Long> ids = pedidos.stream().map(Pedido::getId).toList();
        if (!ids.isEmpty()) pedidoRepository.findAllWithItemsByIdIn(ids);
        return pedidos.map(this::pedidoResponse);
    }

    @Transactional(readOnly = true)
    public Page<AdminAuditResponse> listarAuditoria(Long adminId, String acao, String entidade,
            OffsetDateTime inicio, OffsetDateTime fim, Pageable pageable) {
        return adminAuditRepository.findAll(AdminAuditSpecification.filtros(adminId, acao, entidade, inicio, fim), pageable)
                .map(this::auditResponse);
    }

    private AdminAccountResponse contaResponse(Account account) {
        return new AdminAccountResponse(account.getId(), account.getName(), account.getEmail(), account.getType(),
                Boolean.TRUE.equals(account.getAtivo()), account.getCreatedAt());
    }

    private AdminPedidoResponse pedidoResponse(Pedido pedido) {
        return new AdminPedidoResponse(pedido.getId(), pedido.getAccount().getId(), pedido.getAccount().getName(),
                pedido.getSebo().getId(), pedido.getSebo().getAccount().getName(), pedido.getStatus(),
                pedido.getStatusPagamento(), pedido.getTotal(), pedido.getCreatedAt(), pedido.getUpdatedAt(),
                pedido.getConfirmadoEm(), pedido.getPagoEm(), pedido.getCanceladoEm(), pedido.getReembolsadoEm(),
                pedido.getItens().stream().map(item -> new AdminPedidoResponse.Item(item.getProduto().getId(),
                        item.getTituloSnapshot(), item.getPrecoSnapshot(), item.getQuantidade())).toList());
    }

    private AdminProdutoResponse produtoResponse(Produto produto) {
        return new AdminProdutoResponse(produto.getId(), produto.getTitulo(), produto.getPreco(), produto.getEstoque(),
                produto.getSebo().getId(), produto.getSebo().getAccount().getName(), Boolean.TRUE.equals(produto.getAtivo()),
                produto.getCreatedAt());
    }

    private AdminSeboResponse seboResponse(Sebo sebo, long[] totais) {
        return new AdminSeboResponse(sebo.getId(), sebo.getAccount().getName(), sebo.getAccount().getName(),
                Boolean.TRUE.equals(sebo.getAccount().getAtivo()), sebo.getStatusVerificacao(), sebo.getCreatedAt(),
                sebo.getUltimaAtividade(), totais[0], totais[1]);
    }

    private Map<Long, long[]> contagensProdutos(List<Sebo> sebos) {
        if (sebos.isEmpty()) return Map.of();
        Map<Long, long[]> resultado = new HashMap<>();
        produtoRepository.contarPorSeboEStatus(sebos.stream().map(Sebo::getId).toList()).forEach(row -> {
            long[] totais = resultado.computeIfAbsent((Long) row[0], ignored -> new long[2]);
            totais[Boolean.TRUE.equals(row[1]) ? 0 : 1] = (Long) row[2];
        });
        return resultado;
    }

    private void auditar(Account admin, String acao, String entidade, Long entidadeId, String descricao) {
        adminAuditRepository.save(AdminAudit.builder().admin(admin).acao(acao).entidade(entidade)
                .entidadeId(entidadeId).descricao(descricao).build());
    }

    private AdminAuditResponse auditResponse(AdminAudit audit) {
        return new AdminAuditResponse(audit.getId(), audit.getAdmin().getId(), audit.getAdmin().getName(),
                audit.getAdmin().getEmail(), audit.getAcao(), audit.getEntidade(), audit.getEntidadeId(),
                audit.getDescricao(), audit.getCreatedAt());
    }
}
