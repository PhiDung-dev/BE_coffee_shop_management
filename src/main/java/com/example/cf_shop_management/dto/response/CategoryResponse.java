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
public class CategoryResponse {

    Long id;
    String name;
    String slug;
    String imageUrl;
    Boolean isActive;
    Integer sortOrder;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}