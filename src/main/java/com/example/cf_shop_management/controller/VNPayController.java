package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.response.VNPayReturnResponse;
import com.example.cf_shop_management.dto.response.VNPayUrlResponse;
import com.example.cf_shop_management.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayController {

    VNPayService vnPayService;

    @PostMapping("/create-url/{paymentId}")
    public ResponseEntity<VNPayUrlResponse> createPaymentUrl(
            @PathVariable Long paymentId,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(vnPayService.createPaymentUrl(paymentId, request));
    }

    @GetMapping("/return")
    public ResponseEntity<VNPayReturnResponse> handleVnpayReturn(
            @RequestParam Map<String, String> params
    ) {
        return ResponseEntity.ok(vnPayService.processReturnUrl(params));
    }
}