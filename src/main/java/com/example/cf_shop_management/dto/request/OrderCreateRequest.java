package com.example.cf_shop_management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateRequest {

    @NotBlank(message = "Loại đơn hàng không được để trống")
    String orderType;

    Long tableId;

    Long customerAccountId;

    Long staffAccountId;

    String voucherCode;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid
    List<OrderItemRequest> items;
}
