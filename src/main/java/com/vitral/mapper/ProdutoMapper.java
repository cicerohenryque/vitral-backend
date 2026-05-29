package com.vitral.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vitral.dto.ProdutoResponse;
import com.vitral.entity.Produto;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(target = "seboId", source = "sebo.id")
    ProdutoResponse toResponse(Produto produto);
}
