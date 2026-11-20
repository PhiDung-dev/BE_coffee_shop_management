package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.response.VNPayIpnResponse;
import com.example.cf_shop_management.service.VNPayIpnService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/vnpay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayWebhookController {

    VNPayIpnService vnPayIpnService;

    @GetMapping("/ipn")
    public ResponseEntity<VNPayIpnResponse> handleVnpayIpn(@RequestParam Map<String, String> params) {
        VNPayIpnResponse response = vnPayIpnService.processIpn(params);
        return ResponseEntity.ok(response);
    }
}