package com.example.cf_shop_management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetResponse {

    Long id;
    String token;
    LocalDateTime expiresAt;
    Boolean isUsed;
    Long accountId;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}