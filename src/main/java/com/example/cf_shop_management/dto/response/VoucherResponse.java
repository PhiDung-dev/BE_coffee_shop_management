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
public class VoucherResponse {

    Long id;
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
    Integer usedCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}