package com.example.cf_shop_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateRequest {

    String orderCode;

    String orderType;

    String status;

    Long tableId;

    Long customerAccountId;

    Long staffAccountId;

    Long appliedVoucherId;

    List<OrderItemRequest> items;
}