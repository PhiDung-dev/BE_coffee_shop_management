package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.UserVoucherCreateRequest;
import com.example.cf_shop_management.dto.request.UserVoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.UserVoucherResponse;
import com.example.cf_shop_management.entity.UserVoucher;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserVoucherMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "voucher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserVoucher toUserVoucher(UserVoucherCreateRequest request);

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.email", target = "accountEmail")
    @Mapping(source = "voucher.id", target = "voucherId")
    @Mapping(source = "voucher.code", target = "voucherCode")
    @Mapping(source = "voucher.title", target = "voucherTitle")
    @Mapping(source = "voucher.discountType", target = "discountType")
    @Mapping(source = "voucher.discountValue", target = "discountValue")
    @Mapping(source = "voucher.maxDiscountAmount", target = "maxDiscountAmount")
    @Mapping(source = "voucher.minOrderValue", target = "minOrderValue")
    @Mapping(source = "voucher.endDate", target = "voucherEndDate")
    UserVoucherResponse toUserVoucherResponse(UserVoucher userVoucher);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "voucher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserVoucher(@MappingTarget UserVoucher userVoucher, UserVoucherUpdateRequest request);
}