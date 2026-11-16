package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.PaymentCreateRequest;
import com.example.cf_shop_management.dto.request.PaymentUpdateRequest;
import com.example.cf_shop_management.dto.response.PaymentResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private PaymentResponse pendingPayment;
    private PaymentResponse successPayment;

    @BeforeEach
    void setUp() {
        pendingPayment = PaymentResponse.builder()
                .id(1L)
                .orderId(10L)
                .orderCode("ORD-20261116-8899AA")
                .amount(new BigDecimal("60000.00"))
                .paymentGateway("VNPAY")
                .paymentMethod("EWALLET")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        successPayment = PaymentResponse.builder()
                .id(1L)
                .orderId(10L)
                .orderCode("ORD-20261116-8899AA")
                .amount(new BigDecimal("60000.00"))
                .paymentGateway("VNPAY")
                .paymentMethod("EWALLET")
                .status("SUCCESS")
                .transactionCode("VNP14589632")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPayment_Success() throws Exception {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("60000.00"))
                .paymentGateway("VNPAY")
                .paymentMethod("EWALLET")
                .build();

        when(paymentService.createPayment(any(PaymentCreateRequest.class))).thenReturn(pendingPayment);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.orderId").value(10L))
                .andExpect(jsonPath("$.result.paymentGateway").value("VNPAY"))
                .andExpect(jsonPath("$.result.status").value("PENDING"));
    }

    @Test
    void createPayment_InvalidAmount_Returns4xx() throws Exception {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("999000.00"))
                .paymentGateway("VNPAY")
                .paymentMethod("EWALLET")
                .build();

        when(paymentService.createPayment(any(PaymentCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.PAYMENT_INVALID_AMOUNT));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllPayments_Paged_Success() throws Exception {
        Page<PaymentResponse> pageResult = new PageImpl<>(
                List.of(successPayment),
                PageRequest.of(0, 10),
                1
        );

        when(paymentService.getAllPayments(
                eq(10L),
                eq("SUCCESS"),
                eq("EWALLET"),
                eq("VNPAY"),
                eq("VNP145"),
                any(Pageable.class)
        )).thenReturn(pageResult);

        mockMvc.perform(get("/api/payments")
                        .param("orderId", "10")
                        .param("status", "SUCCESS")
                        .param("paymentMethod", "EWALLET")
                        .param("paymentGateway", "VNPAY")
                        .param("keyword", "VNP145")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].transactionCode").value("VNP14589632"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getPaymentById_Success() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(successPayment);

        mockMvc.perform(get("/api/payments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.status").value("SUCCESS"));
    }

    @Test
    void getPaymentById_NotFound_Returns4xx() throws Exception {
        when(paymentService.getPaymentById(99L))
                .thenThrow(new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        mockMvc.perform(get("/api/payments/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getPaymentsByOrderId_Success() throws Exception {
        when(paymentService.getPaymentsByOrderId(10L)).thenReturn(List.of(successPayment));

        mockMvc.perform(get("/api/payments/order/{orderId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].orderCode").value("ORD-20261116-8899AA"));
    }

    @Test
    void updatePayment_Success() throws Exception {
        PaymentUpdateRequest request = PaymentUpdateRequest.builder()
                .status("SUCCESS")
                .transactionCode("VNP14589632")
                .paidAt(LocalDateTime.now())
                .build();

        when(paymentService.updatePayment(eq(1L), any(PaymentUpdateRequest.class))).thenReturn(successPayment);

        mockMvc.perform(put("/api/payments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("SUCCESS"))
                .andExpect(jsonPath("$.result.transactionCode").value("VNP14589632"));
    }

    @Test
    void updatePayment_AlreadyCompleted_Returns4xx() throws Exception {
        PaymentUpdateRequest request = PaymentUpdateRequest.builder()
                .amount(new BigDecimal("50000.00"))
                .build();

        when(paymentService.updatePayment(eq(1L), any(PaymentUpdateRequest.class)))
                .thenThrow(new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED));

        mockMvc.perform(put("/api/payments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deletePayment_Success() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/api/payments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa giao dịch thanh toán thành công"));

        verify(paymentService, times(1)).deletePayment(1L);
    }

    @Test
    void deletePayment_AlreadyCompleted_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED)).when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/api/payments/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}