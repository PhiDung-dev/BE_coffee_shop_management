package com.example.cf_shop_management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    Long id;
    String orderCode;
    String orderType;
    String status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal finalAmount;

    Long tableId;
    String tableNumber;

    Long customerAccountId;
    String customerEmail;

    Long staffAccountId;
    String staffEmail;

    Long appliedVoucherId;
    String appliedVoucherCode;

    List<OrderItemResponse> orderItems;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
