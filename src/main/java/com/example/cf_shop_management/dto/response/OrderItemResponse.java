package com.example.cf_shop_management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    Long id;
    Long orderId;
    String orderCode;
    Long productId;
    String productName;
    String productImageUrl;
    Integer quantity;
    String selectedOptions;
    BigDecimal unitPrice;
    BigDecimal subtotal;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}