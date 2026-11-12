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
public class ProductResponse {
    Long id;
    String name;
    String slug;
    BigDecimal basePrice;
    String description;
    String imageUrl;
    Boolean isAvailable;
    Long categoryId;
    String categoryName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}