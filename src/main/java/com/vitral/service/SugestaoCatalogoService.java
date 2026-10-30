package com.vitral.service;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.dto.ProdutoRequest;
import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.AprovarSugestaoCatalogoRequest;
import com.vitral.dto.SugestaoCatalogoRequest;
import com.vitral.dto.SugestaoCatalogoResponse;
import com.vitral.entity.Account;
import com.vitral.entity.Categoria;
import com.vitral.entity.Sebo;
import com.vitral.entity.SugestaoCatalogo;
import com.vitral.enumerations.AccountType;
import com.vitral.enumerations.StatusSugestaoCatalogo;
import com.vitral.enumerations.StatusVerificacaoSebo;
import com.vitral.exception.BusinessException;
import com.vitral.exception.ResourceNotFoundException;
import com.vitral.repository.SeboRepository;
import com.vitral.repository.SugestaoCatalogoRepository;
import com.vitral.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SugestaoCatalogoService {

    private final SugestaoCatalogoRepository repository;
    private final SeboRepository seboRepository;
    private final CategoriaService categoriaService;
    private final ProdutoService produtoService;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public SugestaoCatalogoResponse criar(Account usuario, SugestaoCatalogoRequest request) {
        exigirTipo(usuario, AccountType.USUARIO);
        Sebo sebo = seboRepository.findByIdAndAccountAtivoTrueAndStatusVerificacao(
                request.seboId(), StatusVerificacaoSebo.VERIFICADO)
                .orElseThrow(() -> new ResourceNotFoundException("O catálogo de destino não foi encontrado."));
        Categoria categoria = categoriaService.buscarPermitida(request.categoriaId());
        if (request.bookGenre() != null && !"livros".equals(categoria.getSlug())) {
            throw new BusinessException("O gênero literário só pode ser informado para produtos da categoria Livros.", HttpStatus.BAD_REQUEST);
        }
        SugestaoCatalogo sugestao = SugestaoCatalogo.builder()
                .usuario(usuario).sebo(sebo).categoria(categoria)
                .titulo(request.titulo().trim()).autor(trimToNull(request.autor()))
                .descricao(trimToNull(request.descricao())).ano(request.ano()).preco(request.preco())
                .estoque(request.estoque() == null ? 1 : request.estoque()).condicao(request.condicao())
                .bookGenre(request.bookGenre()).fotoUrl(trimToNull(request.fotoUrl())).build();
        return toResponse(repository.save(sugestao));
    }

    @Transactional(readOnly = true)
    public Page<SugestaoCatalogoResponse> listarMinhas(Account usuario, Pageable pageable) {
        exigirTipo(usuario, AccountType.USUARIO);
        return repository.findByUsuarioId(usuario.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SugestaoCatalogoResponse> listarRecebidas(Account responsavel, Pageable pageable) {
        exigirTipo(responsavel, AccountType.SEBO);
        return repository.findBySeboAccountId(responsavel.getId(), pageable).map(this::toResponse);
    }

    @Transactional
    public SugestaoCatalogoResponse aprovar(Account responsavel, Long id, AprovarSugestaoCatalogoRequest revisao) {
        SugestaoCatalogo sugestao = obterPendenteDoResponsavel(responsavel, id);
        if (revisao != null) {
            aplicarRevisao(sugestao, revisao);
        }
        ProdutoResponse produto = produtoService.criar(responsavel, toProdutoRequest(sugestao));
        sugestao.setStatus(StatusSugestaoCatalogo.APROVADA);
        sugestao.setDecididoPor(responsavel);
        sugestao.setDecididoEm(OffsetDateTime.now());
        sugestao.setProdutoCriado(produtoRepository.getReferenceById(produto.id()));
        return toResponse(sugestao);
    }

    public SugestaoCatalogoResponse aprovar(Account responsavel, Long id) {
        return aprovar(responsavel, id, null);
    }

    @Transactional
    public SugestaoCatalogoResponse rejeitar(Account responsavel, Long id, String motivo) {
        SugestaoCatalogo sugestao = obterPendenteDoResponsavel(responsavel, id);
        sugestao.setStatus(StatusSugestaoCatalogo.REJEITADA);
        sugestao.setDecididoPor(responsavel);
        sugestao.setDecididoEm(OffsetDateTime.now());
        sugestao.setMotivoRejeicao(trimToNull(motivo));
        return toResponse(sugestao);
    }

    private SugestaoCatalogo obterPendenteDoResponsavel(Account responsavel, Long id) {
        exigirTipo(responsavel, AccountType.SEBO);
        SugestaoCatalogo sugestao = repository.findByIdParaDecisao(id)
                .orElseThrow(() -> new ResourceNotFoundException("A sugestão informada não foi encontrada."));
        if (!sugestao.getSebo().getAccount().getId().equals(responsavel.getId())) {
            throw new BusinessException("Somente o responsável por este catálogo pode decidir a sugestão.", HttpStatus.FORBIDDEN);
        }
        if (sugestao.getStatus() != StatusSugestaoCatalogo.PENDENTE) {
            throw new BusinessException("Esta sugestão já foi analisada.", HttpStatus.CONFLICT);
        }
        return sugestao;
    }

    private ProdutoRequest toProdutoRequest(SugestaoCatalogo s) {
        return new ProdutoRequest(s.getTitulo(), s.getCategoria().getId(), s.getBookGenre(), s.getAutor(),
                s.getDescricao(), s.getAno(), s.getPreco(), s.getEstoque(), s.getCondicao(), s.getFotoUrl(), false, false);
    }

    private void aplicarRevisao(SugestaoCatalogo sugestao, AprovarSugestaoCatalogoRequest revisao) {
        Categoria categoria = categoriaService.buscarPermitida(revisao.categoriaId());
        if (revisao.bookGenre() != null && !"livros".equals(categoria.getSlug())) {
            throw new BusinessException("O gênero literário só pode ser informado para produtos da categoria Livros.", HttpStatus.BAD_REQUEST);
        }
        sugestao.setCategoria(categoria);
        sugestao.setTitulo(revisao.titulo().trim());
        sugestao.setAutor(trimToNull(revisao.autor()));
        sugestao.setDescricao(trimToNull(revisao.descricao()));
        sugestao.setAno(revisao.ano());
        sugestao.setPreco(revisao.preco());
        sugestao.setEstoque(revisao.estoque());
        sugestao.setCondicao(revisao.condicao());
        sugestao.setBookGenre(revisao.bookGenre());
        sugestao.setFotoUrl(trimToNull(revisao.fotoUrl()));
    }

    private SugestaoCatalogoResponse toResponse(SugestaoCatalogo s) {
        return new SugestaoCatalogoResponse(s.getId(), s.getUsuario().getId(), s.getUsuario().getName(),
                s.getSebo().getId(), s.getSebo().getAccount().getName(), s.getCategoria().getId(),
                s.getCategoria().getNome(), s.getTitulo(), s.getAutor(), s.getDescricao(), s.getAno(), s.getPreco(),
                s.getEstoque(), s.getCondicao(), s.getBookGenre(), s.getFotoUrl(), s.getStatus(), s.getCreatedAt(),
                s.getDecididoEm(), s.getMotivoRejeicao(), s.getProdutoCriado() == null ? null : s.getProdutoCriado().getId());
    }

    private void exigirTipo(Account account, AccountType tipo) {
        if (account.getType() != tipo) {
            throw new BusinessException("Esta conta não possui permissão para realizar a operação.", HttpStatus.FORBIDDEN);
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
