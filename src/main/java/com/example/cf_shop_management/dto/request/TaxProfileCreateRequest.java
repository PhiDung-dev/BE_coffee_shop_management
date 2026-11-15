package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaxProfileCreateRequest {

    @NotBlank(message = "Tên công ty / đơn vị không được để trống")
    @Size(max = 150, message = "Tên công ty tối đa 150 ký tự")
    String companyName;

    @NotBlank(message = "Mã số thuế không được để trống")
    @Size(max = 50, message = "Mã số thuế tối đa 50 ký tự")
    String taxCode;

    @NotBlank(message = "Địa chỉ công ty không được để trống")
    @Size(max = 255, message = "Địa chỉ công ty tối đa 255 ký tự")
    String companyAddress;

    Long accountId;
}