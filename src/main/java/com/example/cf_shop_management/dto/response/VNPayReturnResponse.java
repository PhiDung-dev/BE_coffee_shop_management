package com.example.cf_shop_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayReturnResponse {
    private boolean validSignature;
    private boolean success;
    private String responseCode;
    private String message;
    private Long paymentId;
    private String orderCode;
    private String transactionNo;
    private String bankCode;
    private BigDecimal amount;
    private String paymentStatus;
}