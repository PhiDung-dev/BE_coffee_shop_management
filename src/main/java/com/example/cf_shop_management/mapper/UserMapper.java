package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.UserCreateRequest;
import com.example.cf_shop_management.dto.request.UserUpdateRequest;
import com.example.cf_shop_management.dto.response.UserResponse;
import com.example.cf_shop_management.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    User toUser(UserCreateRequest request);

    @Mapping(target = "accountId", source = "account.id")
    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);
}