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
public class TablesResponse {

    Long id;
    String tableNumber;
    Integer capacity;
    String status;

    Long areaId;
    String areaName;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
