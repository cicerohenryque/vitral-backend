package com.vitral.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vitral.dto.PageResponse;
import com.vitral.dto.ProdutoResponse;
import com.vitral.dto.SeboResponse;
import com.vitral.enumerations.CondicaoProduto;
import com.vitral.enumerations.TipoOperacaoSebo;
import com.vitral.service.BuscaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/busca")
@RequiredArgsConstructor
@Tag(name = "Busca", description = "Busca publica de sebos e produtos sem necessidade de login")
public class BuscaController {

    private final BuscaService buscaService;

    @GetMapping("/sebos")
    @Operation(summary = "Busca vitrines virtuais de sebos ativos pelo nome opcional (q)")
    public ResponseEntity<PageResponse<SeboResponse>> buscarSebos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TipoOperacaoSebo tipoOperacao,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String uf,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(buscaService.buscarSebos(q, tipoOperacao, cidade, uf, pageable)));
    }

    @GetMapping("/produtos")
    @Operation(summary = "Busca produtos ativos com filtros opcionais: titulo (q), seboId, condicao, precoMin e precoMax")
    public ResponseEntity<PageResponse<ProdutoResponse>> buscarProdutos(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long seboId,
            @RequestParam(required = false) CondicaoProduto condicao,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(buscaService.buscarProdutos(q, seboId, condicao, precoMin, precoMax, pageable)));
    }
}
