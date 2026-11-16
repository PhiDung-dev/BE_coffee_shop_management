package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.response.PasswordResetResponse;
import com.example.cf_shop_management.entity.PasswordReset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PasswordResetMapper {

    @Mapping(source = "account.id", target = "accountId")
    PasswordResetResponse toPasswordResetResponse(PasswordReset passwordReset);
}