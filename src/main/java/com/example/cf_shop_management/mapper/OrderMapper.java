package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

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

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrl", target = "productImageUrl")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(OrderUpdateRequest request, @MappingTarget Order order);
}
