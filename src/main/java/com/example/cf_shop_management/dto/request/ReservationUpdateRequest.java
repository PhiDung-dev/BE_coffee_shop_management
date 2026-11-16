package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class ReservationUpdateRequest {
    Long accountId;

    Integer guestCount;

    LocalDateTime reservationTime;

    String note;

    String status;

    List<Long> tableIds;
}