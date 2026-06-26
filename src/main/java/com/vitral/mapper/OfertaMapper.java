package com.vitral.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vitral.dto.OfertaResponse;
import com.vitral.entity.Oferta;

@Mapper(componentModel = "spring")
public interface OfertaMapper {

    @Mapping(target = "produtoId", source = "produto.id")
    @Mapping(target = "seboId", source = "produto.sebo.id")
    @Mapping(target = "tituloProduto", source = "produto.titulo")
    OfertaResponse toResponse(Oferta oferta);
}
