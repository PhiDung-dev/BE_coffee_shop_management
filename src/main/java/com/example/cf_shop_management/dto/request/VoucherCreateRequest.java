package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VoucherCreateRequest {

    String title;

    @NotBlank(message = "Mã voucher không được để trống")
    String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @Min(value = 0, message = "Giá trị giảm phải lớn hơn hoặc bằng 0")
    BigDecimal discountValue;

    BigDecimal maxDiscountAmount;

    BigDecimal minOrderValue;

    LocalDateTime startDate;

    LocalDateTime endDate;

    Boolean isActive;

    Integer usageLimit;
}