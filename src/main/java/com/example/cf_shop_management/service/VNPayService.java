package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.response.VNPayReturnResponse;
import com.example.cf_shop_management.dto.response.VNPayUrlResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.Payment;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.repository.PaymentRepository;
import com.example.cf_shop_management.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayService {

    PaymentRepository paymentRepository;

    @NonFinal
    @Value("${vnpay.tmn-code}")
    String vnpTmnCode;

    @NonFinal
    @Value("${vnpay.hash-secret}")
    String vnpHashSecret;

    @NonFinal
    @Value("${vnpay.pay-url}")
    String vnpPayUrl;

    @NonFinal
    @Value("${vnpay.return-url}")
    String vnpReturnUrl;

    @Transactional(readOnly = true)
    public VNPayUrlResponse createPaymentUrl(Long paymentId, HttpServletRequest httpRequest) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        Order order = payment.getOrder();
        if (order != null && "CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
        }

        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }

        // VNPay tính đơn vị VND * 100 để tạo mã QR khóa đúng số tiền
        long amountInVndCents = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnpCreateDate = now.format(formatter);
        String vnpExpireDate = now.plusMinutes(15).format(formatter);

        String orderCode = (order != null && order.getOrderCode() != null)
                ? order.getOrderCode()
                : "PAYMENT-" + payment.getId();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amountInVndCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(payment.getId()));
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderCode);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", getIpAddress(httpRequest));
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

                hashData.append(fieldName).append('=').append(encodedValue);
                query.append(encodedName).append('=').append(encodedValue);

                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpHashSecret, hashData.toString());
        String fullPaymentUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + vnpSecureHash;

        return VNPayUrlResponse.builder()
                .paymentId(payment.getId())
                .paymentUrl(fullPaymentUrl)
                .build();
    }

    @Transactional(readOnly = true)
    public VNPayReturnResponse processReturnUrl(Map<String, String> queryParams) {
        String receivedHash = queryParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return VNPayReturnResponse.builder()
                    .validSignature(false)
                    .success(false)
                    .message("Thiếu chữ ký bảo mật từ VNPay")
                    .build();
        }

        Map<String, String> fieldsToHash = new HashMap<>(queryParams);
        fieldsToHash.remove("vnp_SecureHash");
        fieldsToHash.remove("vnp_SecureHashType");

        String calculatedHash = VNPayUtil.hashAllFields(fieldsToHash, vnpHashSecret);
        if (!calculatedHash.equalsIgnoreCase(receivedHash)) {
            return VNPayReturnResponse.builder()
                    .validSignature(false)
                    .success(false)
                    .message("Chữ ký giao dịch không hợp lệ (Invalid Checksum)")
                    .build();
        }

        String responseCode = queryParams.get("vnp_ResponseCode");
        String transactionStatus = queryParams.get("vnp_TransactionStatus");
        String transactionNo = queryParams.get("vnp_TransactionNo");
        String bankCode = queryParams.get("vnp_BankCode");

        Long paymentId = parsePaymentId(queryParams.get("vnp_TxnRef"));
        Payment payment = (paymentId != null)
                ? paymentRepository.findById(paymentId).orElse(null)
                : null;

        long rawAmount = Long.parseLong(queryParams.getOrDefault("vnp_Amount", "0"));
        BigDecimal amount = BigDecimal.valueOf(rawAmount).divide(BigDecimal.valueOf(100));

        boolean isSuccess = "00".equals(responseCode) && "00".equals(transactionStatus);

        return VNPayReturnResponse.builder()
                .validSignature(true)
                .success(isSuccess)
                .responseCode(responseCode)
                .message(isSuccess ? "Thanh toán VNPay thành công" : mapVnpResponseMessage(responseCode))
                .paymentId(paymentId)
                .orderCode(payment != null && payment.getOrder() != null ? payment.getOrder().getOrderCode() : null)
                .transactionNo(transactionNo)
                .bankCode(bankCode)
                .amount(amount)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .build();
    }

    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null && !ipAddress.isBlank()) {
            ipAddress = ipAddress.split(",")[0].trim();
        } else {
            ipAddress = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }
        return (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : "127.0.0.1";
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

    private String mapVnpResponseMessage(String code) {
        if (code == null) {
            return "Giao dịch không thành công";
        }
        return switch (code) {
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09" -> "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10" -> "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán. Vui lòng thực hiện lại giao dịch";
            case "12" -> "Thẻ/Tài khoản của khách hàng bị khóa";
            case "13" -> "Nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng đã hủy giao dịch";
            case "51" -> "Tài khoản không đủ số dư để thực hiện giao dịch";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            default -> "Thanh toán thất bại (Mã lỗi VNPay: " + code + ")";
        };
    }
}