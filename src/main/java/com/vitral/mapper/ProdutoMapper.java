package com.vitral.mapper;

import java.math.BigDecimal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vitral.dto.ProdutoResponse;
import com.vitral.entity.Produto;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(target = "seboId", source = "produto.sebo.id")
    @Mapping(target = "categoriaId", source = "produto.categoria.id")
    @Mapping(target = "categoriaNome", source = "produto.categoria.nome")
    @Mapping(target = "catalogoLivroId", source = "produto.catalogoLivro.id")
    @Mapping(target = "isbn", source = "produto.catalogoLivro.isbn")
    @Mapping(target = "precoPromocional", source = "precoPromocional")
    ProdutoResponse toResponse(Produto produto, BigDecimal precoPromocional);
}
