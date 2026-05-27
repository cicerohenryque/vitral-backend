package com.vitral.mapper;

import org.mapstruct.Mapper;

import com.vitral.dto.AccountResponse;
import com.vitral.entity.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse toResponse(Account account);
}
