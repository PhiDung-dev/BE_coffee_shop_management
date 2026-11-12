package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.OrderItemCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderItemResponse;
import com.example.cf_shop_management.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderItem toOrderItem(OrderItemCreateRequest request);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrl", target = "productImageUrl")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateOrderItem(@MappingTarget OrderItem orderItem, OrderItemUpdateRequest request);
}