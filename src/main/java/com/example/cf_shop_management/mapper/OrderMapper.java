package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.entity.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderCode", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "customerAccount", ignore = true)
    @Mapping(target = "staffAccount", ignore = true)
    @Mapping(target = "appliedVoucher", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toOrder(OrderCreateRequest request);

    @Mapping(source = "table.id", target = "tableId")
    @Mapping(source = "table.tableNumber", target = "tableNumber")
    @Mapping(source = "customerAccount.id", target = "customerAccountId")
    @Mapping(source = "customerAccount.email", target = "customerEmail")
    @Mapping(source = "staffAccount.id", target = "staffAccountId")
    @Mapping(source = "staffAccount.email", target = "staffEmail")
    @Mapping(source = "appliedVoucher.id", target = "appliedVoucherId")
    @Mapping(source = "appliedVoucher.code", target = "appliedVoucherCode")
    OrderResponse toOrderResponse(Order order);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderCode", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "finalAmount", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "customerAccount", ignore = true)
    @Mapping(target = "staffAccount", ignore = true)
    @Mapping(target = "appliedVoucher", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateOrder(OrderUpdateRequest request, @MappingTarget Order order);
}