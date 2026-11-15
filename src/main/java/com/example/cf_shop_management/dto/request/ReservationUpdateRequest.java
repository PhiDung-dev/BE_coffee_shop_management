package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "Số lượng khách phải lớn hơn hoặc bằng 1")
    Integer guestCount;

    String note;

    LocalDateTime reservationTime;

    String status; // VD: PENDING, CONFIRMED, SEATED, COMPLETED, CANCELLED

    List<Long> tableIds;
}
