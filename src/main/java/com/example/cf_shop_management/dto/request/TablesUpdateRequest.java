package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TablesUpdateRequest {

    String tableNumber;

    @Min(value = 1, message = "Sức chứa phải lớn hơn hoặc bằng 1")
    Integer capacity;

    String status;

    Long areaId;
}
