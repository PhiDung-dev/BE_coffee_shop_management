package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVoucherCreateRequest {

    @NotNull(message = "Mã tài khoản không được để trống")
    Long accountId;

    @NotNull(message = "Mã voucher không được để trống")
    Long voucherId;

    Boolean isUsed;

    LocalDateTime usedAt;
}