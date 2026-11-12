package com.example.cf_shop_management.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVoucherUpdateRequest {

    Boolean isUsed;

    LocalDateTime usedAt;
}