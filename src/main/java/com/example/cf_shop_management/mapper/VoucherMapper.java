package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.VoucherCreateRequest;
import com.example.cf_shop_management.dto.request.VoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.VoucherResponse;
import com.example.cf_shop_management.entity.Voucher;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VoucherMapper {

    Voucher toVoucher(VoucherCreateRequest request);

    VoucherResponse toVoucherResponse(Voucher voucher);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVoucher(VoucherUpdateRequest request, @MappingTarget Voucher voucher);
}