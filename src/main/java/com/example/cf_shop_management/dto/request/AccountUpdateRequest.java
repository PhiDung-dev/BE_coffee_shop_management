package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountUpdateRequest {
    @Email(message = "Email không đúng định dạng")
    String email;

    String phone;

    String password;

    String status;

    Long roleId;
}