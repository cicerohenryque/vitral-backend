package com.vitral.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vitral.dto.SeboResponse;
import com.vitral.entity.Sebo;

@Mapper(componentModel = "spring")
public interface SeboMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "nome", source = "account.name")
    @Mapping(target = "email", source = "account.email")
    @Mapping(target = "distanciaKm", ignore = true)
    SeboResponse toResponse(Sebo sebo);

    @Mapping(target = "accountId", source = "sebo.account.id")
    @Mapping(target = "nome", source = "sebo.account.name")
    @Mapping(target = "email", source = "sebo.account.email")
    @Mapping(target = "distanciaKm", source = "distanciaKm")
    SeboResponse toResponse(Sebo sebo, Double distanciaKm);
}
