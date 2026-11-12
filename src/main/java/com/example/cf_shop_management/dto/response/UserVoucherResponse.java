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
public class UserVoucherResponse {
    Long id;
    Long accountId;
    String accountEmail;
    Long voucherId;
    String voucherCode;
    String voucherTitle;
    String discountType;
    BigDecimal discountValue;
    BigDecimal maxDiscountAmount;
    BigDecimal minOrderValue;
    LocalDateTime voucherEndDate;
    Boolean isUsed;
    LocalDateTime usedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}