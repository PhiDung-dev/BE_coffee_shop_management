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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
        }

        if (order.getFinalAmount() != null) {
            BigDecimal totalPaid = paymentRepository.sumPaidAmountByOrderId(order.getId());
            BigDecimal remainingAmount = order.getFinalAmount().subtract(totalPaid);

            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
            }
            if (request.getAmount().compareTo(remainingAmount) > 0) {
                throw new AppException(ErrorCode.PAYMENT_INVALID_AMOUNT);
            }
        }

        Payment payment = paymentMapper.toPayment(request);
        payment.setOrder(order);
        payment.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase());

        if (request.getPaymentGateway() != null && !request.getPaymentGateway().isBlank()) {
            payment.setPaymentGateway(request.getPaymentGateway().trim().toUpperCase());
        }

        payment.setStatus("PENDING");

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(
            Long orderId,
            String status,
            String paymentMethod,
            String paymentGateway,
            String keyword,
            Pageable pageable
    ) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return paymentRepository.searchPayments(
                        orderId,
                        status,
                        paymentMethod,
                        paymentGateway,
                        normalizedKeyword,
                        pageable
                )
                .map(paymentMapper::toPaymentResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(paymentMapper::toPaymentResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        boolean isCurrentlySuccess = "SUCCESS".equalsIgnoreCase(payment.getStatus());

        if (isCurrentlySuccess && (request.getAmount() != null
                || request.getPaymentMethod() != null
                || request.getPaymentGateway() != null)) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        if (request.getTransactionCode() != null && !request.getTransactionCode().isBlank()) {
            String trimmedTxCode = request.getTransactionCode().trim();
            if (paymentRepository.existsByTransactionCodeAndIdNot(trimmedTxCode, id)) {
                throw new AppException(ErrorCode.PAYMENT_TRANSACTION_EXISTED);
            }
            request.setTransactionCode(trimmedTxCode);
        }

        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            request.setPaymentMethod(request.getPaymentMethod().trim().toUpperCase());
        }
        if (request.getPaymentGateway() != null && !request.getPaymentGateway().isBlank()) {
            request.setPaymentGateway(request.getPaymentGateway().trim().toUpperCase());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            request.setStatus(request.getStatus().trim().toUpperCase());
        }

        paymentMapper.updatePayment(request, payment);

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus()) && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new AppException(ErrorCode.PAYMENT_ALREADY_COMPLETED);
        }

        paymentRepository.delete(payment);
    }
}