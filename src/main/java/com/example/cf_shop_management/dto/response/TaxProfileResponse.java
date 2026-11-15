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
public class TaxProfileResponse {
    Long id;
    String companyName;
    String taxCode;
    String companyAddress;
    Long accountId;
    String accountEmail;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}