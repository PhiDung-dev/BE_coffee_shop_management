package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemCreateRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    Long orderId;

    @NotNull(message = "Mã sản phẩm không được để trống")
    Long productId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    Integer quantity;

    // Lưu cấu hình tùy chọn (VD: JSON hoặc chuỗi "Size L, 50% đường, 70% đá, Thêm trân châu")
    String selectedOptions;

    // Cho phép truyền đơn giá đã cộng topping; nếu null sẽ tự lấy basePrice của Product
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0")
    BigDecimal unitPrice;
}