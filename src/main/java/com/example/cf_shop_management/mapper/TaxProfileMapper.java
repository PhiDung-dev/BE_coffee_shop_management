package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.TaxProfileCreateRequest;
import com.example.cf_shop_management.dto.request.TaxProfileUpdateRequest;
import com.example.cf_shop_management.dto.response.TaxProfileResponse;
import com.example.cf_shop_management.entity.TaxProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TaxProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TaxProfile toTaxProfile(TaxProfileCreateRequest request);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.email", target = "accountEmail")
    TaxProfileResponse toTaxProfileResponse(TaxProfile taxProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateTaxProfile(TaxProfileUpdateRequest request, @MappingTarget TaxProfile taxProfile);
}