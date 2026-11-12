package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentUpdateRequest {

    @DecimalMin(value = "0.01", inclusive = true, message = "Số tiền thanh toán phải lớn hơn 0")
    BigDecimal amount;

    @Size(max = 50, message = "Cổng thanh toán tối đa 50 ký tự")
    String paymentGateway;

    @Size(max = 20, message = "Phương thức thanh toán tối đa 20 ký tự")
    String paymentMethod;

    @Size(max = 20, message = "Trạng thái tối đa 20 ký tự")
    String status;

    @Size(max = 100, message = "Mã giao dịch tối đa 100 ký tự")
    String transactionCode;

    LocalDateTime paidAt;
}