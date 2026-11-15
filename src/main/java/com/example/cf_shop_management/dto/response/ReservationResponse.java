package com.example.cf_shop_management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationResponse {

    Long id;
    Integer guestCount;
    String note;
    LocalDateTime reservationTime;
    String status;

    Long accountId;
    String accountEmail;
    String accountPhone;

    List<TablesResponse> tables;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
