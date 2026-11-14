package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TablesCreateRequest {

    @NotBlank(message = "Số bàn không được để trống")
    String tableNumber;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn hoặc bằng 1")
    Integer capacity;

    String status; // VD: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE (mặc định AVAILABLE nếu null)

    @NotNull(message = "ID khu vực không được để trống")
    Long areaId;
}
