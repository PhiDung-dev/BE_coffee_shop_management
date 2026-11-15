package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaxProfileUpdateRequest {

    @Size(max = 150, message = "Tên công ty tối đa 150 ký tự")
    String companyName;

    @Size(max = 50, message = "Mã số thuế tối đa 50 ký tự")
    String taxCode;

    @Size(max = 255, message = "Địa chỉ công ty tối đa 255 ký tự")
    String companyAddress;

    Long accountId;
}