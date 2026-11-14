package com.example.cf_shop_management.dto.response;

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
public class InvoiceResponse {

    Long id;
    String invoiceNumber;
    LocalDateTime issuedAt;
    String pdfUrl;

    Long orderId;
    String orderCode;
    BigDecimal finalAmount;

    Long taxProfileId;
    String companyName;
    String taxCode;
    String companyAddress;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
