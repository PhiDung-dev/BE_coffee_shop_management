package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.PaymentCreateRequest;
import com.example.cf_shop_management.dto.request.PaymentUpdateRequest;
import com.example.cf_shop_management.dto.response.PaymentResponse;
import com.example.cf_shop_management.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createPayment(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String paymentGateway,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<PaymentResponse>>builder()
                .result(paymentService.getAllPayments(orderId, status, paymentMethod, paymentGateway, keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.getPaymentById(id))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable Long orderId) {
        return ApiResponse.<List<PaymentResponse>>builder()
                .result(paymentService.getPaymentsByOrderId(orderId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PaymentResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentUpdateRequest request
    ) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.updatePayment(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ApiResponse.<String>builder()
                .result("Xóa giao dịch thanh toán thành công")
                .build();
    }
}