package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PaymentCreateRequest {

    @NotNull(message = "Mã đơn hàng không được để trống")
    Long orderId;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "0.01", inclusive = true, message = "Số tiền thanh toán phải lớn hơn 0")
    BigDecimal amount;

    @Size(max = 50, message = "Cổng thanh toán tối đa 50 ký tự")
    String paymentGateway; // VD: VNPAY, MOMO, ZALOPAY, CASH

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    @Size(max = 20, message = "Phương thức thanh toán tối đa 20 ký tự")
    String paymentMethod; // VD: CASH, QR, CARD, TRANSFER

    @Size(max = 20, message = "Trạng thái tối đa 20 ký tự")
    String status; // VD: PENDING, SUCCESS, FAILED, REFUNDED

    @Size(max = 100, message = "Mã giao dịch tối đa 100 ký tự")
    String transactionCode;

    LocalDateTime paidAt;
}