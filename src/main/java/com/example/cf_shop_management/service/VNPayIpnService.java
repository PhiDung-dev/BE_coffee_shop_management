package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.PaymentUpdateRequest;
import com.example.cf_shop_management.dto.response.VNPayIpnResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.Payment;
import com.example.cf_shop_management.entity.WebhookLog;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.PaymentRepository;
import com.example.cf_shop_management.repository.WebhookLogRepository;
import com.example.cf_shop_management.util.VNPayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayIpnService {

    WebhookLogRepository webhookLogRepository;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    PaymentService paymentService;
    ObjectMapper objectMapper;

    @NonFinal
    @Value("${vnpay.hash-secret}")
    String vnpHashSecret;

    public VNPayIpnResponse processIpn(Map<String, String> queryParams) {
        // BƯỚC 1: Lưu ngay toàn bộ dữ liệu gốc vào webhook_logs (RECEIVED)
        WebhookLog webhookLog = WebhookLog.builder()
                .gateway("VNPAY")
                .payload(serializePayload(queryParams))
                .status("RECEIVED")
                .build();
        webhookLog = webhookLogRepository.save(webhookLog);

        // BƯỚC 2: Kiểm tra chữ ký, đối chiếu số tiền và cập nhật PaymentService
        try {
            if (!isValidSignature(queryParams)) {
                markLogFailed(webhookLog, "Invalid VNPay checksum (vnp_SecureHash mismatch)");
                return VNPayIpnResponse.of("97", "Invalid Checksum");
            }

            String txnRef = queryParams.get("vnp_TxnRef");
            Long paymentId = parsePaymentId(txnRef);
            if (paymentId == null) {
                markLogFailed(webhookLog, "Invalid vnp_TxnRef format: " + txnRef);
                return VNPayIpnResponse.of("01", "Order not Found");
            }

            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                markLogFailed(webhookLog, "Payment not found for id: " + paymentId);
                return VNPayIpnResponse.of("01", "Order not Found");
            }

            // Đối chiếu số tiền (VNPay gửi đơn vị VND * 100)
            long vnpAmountRaw = Long.parseLong(queryParams.getOrDefault("vnp_Amount", "0"));
            BigDecimal vnpAmount = BigDecimal.valueOf(vnpAmountRaw).divide(BigDecimal.valueOf(100));
            if (payment.getAmount() == null || payment.getAmount().compareTo(vnpAmount) != 0) {
                markLogFailed(webhookLog, "Invalid amount. Expected: " + payment.getAmount() + ", Received: " + vnpAmount);
                return VNPayIpnResponse.of("04", "Invalid Amount");
            }

            // Chống xử lý trùng lặp nếu VNPay bắn IPN nhiều lần
            if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                markLogProcessed(webhookLog);
                return VNPayIpnResponse.of("02", "Order already confirmed");
            }

            String responseCode = queryParams.get("vnp_ResponseCode");
            String transactionStatus = queryParams.get("vnp_TransactionStatus");
            String vnpTransactionNo = queryParams.get("vnp_TransactionNo");

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                PaymentUpdateRequest updateRequest = PaymentUpdateRequest.builder()
                        .status("SUCCESS")
                        .transactionCode(vnpTransactionNo)
                        .build();
                paymentService.updatePayment(payment.getId(), updateRequest);

                syncOrderStatusIfFullyPaid(payment.getOrder());
            } else {
                PaymentUpdateRequest updateRequest = PaymentUpdateRequest.builder()
                        .status("FAILED")
                        .transactionCode(vnpTransactionNo)
                        .build();
                paymentService.updatePayment(payment.getId(), updateRequest);
            }

            markLogProcessed(webhookLog);
            return VNPayIpnResponse.of("00", "Confirm Success");

        } catch (Exception e) {
            log.error("Lỗi xử lý VNPay IPN cho logId={}: {}", webhookLog.getId(), e.getMessage(), e);
            markLogFailed(webhookLog, e.getMessage());
            return VNPayIpnResponse.of("99", "Unknown error");
        }
    }

    private boolean isValidSignature(Map<String, String> queryParams) {
        String receivedHash = queryParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> fieldsToHash = new HashMap<>(queryParams);
        fieldsToHash.remove("vnp_SecureHash");
        fieldsToHash.remove("vnp_SecureHashType");

        String calculatedHash = VNPayUtil.hashAllFields(fieldsToHash, vnpHashSecret);
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    private void syncOrderStatusIfFullyPaid(Order order) {
        if (order == null || order.getFinalAmount() == null) {
            return;
        }
        BigDecimal totalPaid = paymentRepository.sumPaidAmountByOrderId(order.getId());
        if (totalPaid.compareTo(order.getFinalAmount()) >= 0
                && "PENDING".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
        }
    }

    private Long parsePaymentId(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(txnRef.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String serializePayload(Map<String, String> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return params.toString();
        }
    }

    private void markLogProcessed(WebhookLog webhookLog) {
        webhookLog.setStatus("PROCESSED");
        webhookLog.setErrorMessage(null);
        webhookLogRepository.save(webhookLog);
    }

    private void markLogFailed(WebhookLog webhookLog, String errorMessage) {
        webhookLog.setStatus("FAILED");
        webhookLog.setErrorMessage(errorMessage);
        webhookLogRepository.save(webhookLog);
    }
}