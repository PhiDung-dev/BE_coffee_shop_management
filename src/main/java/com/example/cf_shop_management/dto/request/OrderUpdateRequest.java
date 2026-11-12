package com.example.cf_shop_management.dto.request;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderUpdateRequest {

    String orderType;

    String status; // VD: PENDING, PREPARING, SERVED, COMPLETED, CANCELLED

    Long tableId;

    Long staffAccountId;

    String voucherCode;

    @Valid
    List<OrderItemRequest> items;
}
