package com.example.cf_shop_management.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    String fullName;

    String avatarUrl;

    LocalDate dob;

    String gender;

    Long accountId;
}