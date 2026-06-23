package com.vitral.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vitral.dto.ProdutoResponse;
import com.vitral.entity.Produto;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(target = "seboId", source = "sebo.id")
    @Mapping(target = "categoriaId", source = "categoria.id")
    @Mapping(target = "categoriaNome", source = "categoria.nome")
    ProdutoResponse toResponse(Produto produto);
}
