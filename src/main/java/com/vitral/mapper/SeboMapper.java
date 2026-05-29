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
    SeboResponse toResponse(Sebo sebo);
}
