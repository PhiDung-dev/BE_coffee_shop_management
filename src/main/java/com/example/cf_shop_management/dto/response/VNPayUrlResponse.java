package com.example.cf_shop_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayUrlResponse {
    private Long paymentId;
    private String paymentUrl;
}