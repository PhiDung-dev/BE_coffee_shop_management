package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemUpdateRequest {

    Long productId;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    Integer quantity;

    String selectedOptions;

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0")
    BigDecimal unitPrice;
}