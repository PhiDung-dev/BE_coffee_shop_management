package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.PaymentCreateRequest;
import com.example.cf_shop_management.dto.request.PaymentUpdateRequest;
import com.example.cf_shop_management.dto.response.PaymentResponse;
import com.example.cf_shop_management.entity.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toPayment(PaymentCreateRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    PaymentResponse toPaymentResponse(Payment payment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePayment(@MappingTarget Payment payment, PaymentUpdateRequest request);
}