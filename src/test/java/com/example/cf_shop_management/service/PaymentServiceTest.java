package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.PaymentCreateRequest;
import com.example.cf_shop_management.dto.request.PaymentUpdateRequest;
import com.example.cf_shop_management.dto.response.PaymentResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.Payment;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.PaymentMapper;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private Payment payment;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(10L)
                .orderCode("ORD-20261116-8899AA")
                .status("PENDING")
                .finalAmount(new BigDecimal("100000.00"))
                .build();

        payment = Payment.builder()
                .id(1L)
                .order(order)
                .amount(new BigDecimal("60000.00"))
                .paymentMethod("EWALLET")
                .paymentGateway("VNPAY")
                .status("PENDING")
                .build();

        paymentResponse = PaymentResponse.builder()
                .id(1L)
                .orderId(10L)
                .amount(new BigDecimal("60000.00"))
                .paymentMethod("EWALLET")
                .paymentGateway("VNPAY")
                .status("PENDING")
                .build();
    }

    @Test
    void createPayment_Success() {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("60000.00"))
                .paymentMethod("  ewallet  ")
                .paymentGateway("  vnpay  ")
                .build();

        Payment mappedPayment = new Payment();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.sumPaidAmountByOrderId(10L)).thenReturn(new BigDecimal("40000.00"));
        when(paymentMapper.toPayment(request)).thenReturn(mappedPayment);
        when(paymentRepository.save(mappedPayment)).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertNotNull(result);
        assertEquals(order, mappedPayment.getOrder());
        assertEquals("EWALLET", mappedPayment.getPaymentMethod());
        assertEquals("VNPAY", mappedPayment.getPaymentGateway());
        assertEquals("PENDING", mappedPayment.getStatus());
        verify(paymentRepository).save(mappedPayment);
    }

    @Test
    void createPayment_OrderCancelled_ThrowsException() {
        order.setStatus("CANCELLED");
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("50000.00"))
                .paymentMethod("CASH")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.createPayment(request));

        assertEquals(ErrorCode.ORDER_CANNOT_BE_MODIFIED, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_OrderAlreadyPaid_ThrowsException() {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("10000.00"))
                .paymentMethod("CASH")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.sumPaidAmountByOrderId(10L)).thenReturn(new BigDecimal("100000.00"));

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.createPayment(request));

        assertEquals(ErrorCode.ORDER_ALREADY_PAID, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_AmountExceedsRemaining_ThrowsException() {
        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .orderId(10L)
                .amount(new BigDecimal("70000.00")) // remaining is 60.000
                .paymentMethod("CASH")
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.sumPaidAmountByOrderId(10L)).thenReturn(new BigDecimal("40000.00"));

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.createPayment(request));

        assertEquals(ErrorCode.PAYMENT_INVALID_AMOUNT, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getAllPayments_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);

        when(paymentRepository.searchPayments(eq(10L), eq("PENDING"), eq("EWALLET"), eq("VNPAY"),
                eq("VNP"), eq(pageable))).thenReturn(page);
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        Page<PaymentResponse> result = paymentService.getAllPayments(
                10L, "PENDING", "EWALLET", "VNPAY", "  VNP  ", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getPaymentById_NotFound_ThrowsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.getPaymentById(99L));

        assertEquals(ErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getPaymentsByOrderId_Success() {
        when(orderRepository.existsById(10L)).thenReturn(true);
        when(paymentRepository.findByOrderIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(10L);

        assertEquals(1, result.size());
    }

    @Test
    void updatePayment_MarkSuccess_AutoSetsPaidAt_Success() {
        PaymentUpdateRequest request = PaymentUpdateRequest.builder()
                .status("  success  ")
                .transactionCode("  VNP14589632  ")
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByTransactionCodeAndIdNot("VNP14589632", 1L)).thenReturn(false);
        doAnswer(inv -> {
            payment.setStatus("SUCCESS");
            payment.setTransactionCode("VNP14589632");
            return null;
        }).when(paymentMapper).updatePayment(request, payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.updatePayment(1L, request);

        assertNotNull(result);
        assertEquals("SUCCESS", payment.getStatus());
        assertNotNull(payment.getPaidAt());
        verify(paymentRepository).save(payment);
    }

    @Test
    void updatePayment_AlreadySuccessChangeAmount_ThrowsException() {
        payment.setStatus("SUCCESS");
        PaymentUpdateRequest request = PaymentUpdateRequest.builder()
                .amount(new BigDecimal("50000.00"))
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.updatePayment(1L, request));

        assertEquals(ErrorCode.PAYMENT_ALREADY_COMPLETED, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void updatePayment_TransactionCodeExisted_ThrowsException() {
        PaymentUpdateRequest request = PaymentUpdateRequest.builder()
                .transactionCode("VNP_DUP")
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.existsByTransactionCodeAndIdNot("VNP_DUP", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.updatePayment(1L, request));

        assertEquals(ErrorCode.PAYMENT_TRANSACTION_EXISTED, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void deletePayment_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).delete(payment);

        paymentService.deletePayment(1L);

        verify(paymentRepository).delete(payment);
    }

    @Test
    void deletePayment_AlreadySuccess_ThrowsException() {
        payment.setStatus("SUCCESS");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        AppException exception = assertThrows(AppException.class,
                () -> paymentService.deletePayment(1L));

        assertEquals(ErrorCode.PAYMENT_ALREADY_COMPLETED, exception.getErrorCode());
        verify(paymentRepository, never()).delete(any());
    }
}