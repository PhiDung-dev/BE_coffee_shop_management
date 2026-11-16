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

    String code;

    String discountType;

    BigDecimal discountValue;

    BigDecimal maxDiscountAmount;

    BigDecimal minOrderValue;

    LocalDateTime startDate;

    LocalDateTime endDate;

    Boolean isActive;

    Integer usageLimit;
}