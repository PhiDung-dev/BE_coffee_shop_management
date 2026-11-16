package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.OrderItem;
import com.example.cf_shop_management.entity.Product;
import com.example.cf_shop_management.entity.Tables;
import com.example.cf_shop_management.entity.Voucher;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.OrderItemMapper;
import com.example.cf_shop_management.mapper.OrderMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import com.example.cf_shop_management.repository.TablesRepository;
import com.example.cf_shop_management.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    TablesRepository tablesRepository;
    AccountRepository accountRepository;
    VoucherRepository voucherRepository;
    ProductRepository productRepository;
    OrderMapper orderMapper;
    OrderItemMapper orderItemMapper;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Order order = orderMapper.toOrder(request);
        order.setOrderCode(generateUniqueOrderCode());
        if (request.getOrderType() != null && !request.getOrderType().isBlank()) {
            order.setOrderType(request.getOrderType().trim().toUpperCase());
        }
        order.setStatus((request.getStatus() != null && !request.getStatus().isBlank())
                ? request.getStatus().trim().toUpperCase()
                : "PENDING");

        if (request.getTableId() != null) {
            Tables table = tablesRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
            if ("MAINTENANCE".equalsIgnoreCase(table.getStatus()) || "DISABLED".equalsIgnoreCase(table.getStatus())) {
                throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
            }
            order.setTable(table);
        }

        if (request.getCustomerAccountId() != null) {
            Account customer = accountRepository.findById(request.getCustomerAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_ACCOUNT_NOT_FOUND));
            order.setCustomerAccount(customer);
        }

        if (request.getStaffAccountId() != null) {
            Account staff = accountRepository.findById(request.getStaffAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_ACCOUNT_NOT_FOUND));
            order.setStaffAccount(staff);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            for (OrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                if (Boolean.FALSE.equals(product.getIsAvailable())) {
                    throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
                }

                BigDecimal unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
                BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

                OrderItem orderItem = orderItemMapper.toOrderItem(itemReq);
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setSubtotal(itemSubtotal);

                order.getOrderItems().add(orderItem);
                subtotal = subtotal.add(itemSubtotal);
            }
        }
        order.setSubtotal(subtotal);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getAppliedVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(request.getAppliedVoucherId())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
            discountAmount = validateAndCalculateVoucherDiscount(voucher, subtotal);
            order.setAppliedVoucher(voucher);

            int currentUsed = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
            voucher.setUsedCount(currentUsed + 1);
            voucherRepository.save(voucher);
        }

        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(subtotal.subtract(discountAmount).max(BigDecimal.ZERO));

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(
            String status,
            String orderType,
            Long tableId,
            Long customerAccountId,
            Long staffAccountId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String keyword,
            Pageable pageable
    ) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return orderRepository.searchOrders(
                        status,
                        orderType,
                        tableId,
                        customerAccountId,
                        staffAccountId,
                        fromDate,
                        toDate,
                        normalizedKeyword,
                        pageable
                )
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCodeIgnoreCase(orderCode.trim())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String currentStatus = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if ("COMPLETED".equals(currentStatus) || "CANCELLED".equals(currentStatus)) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
        }

        if (request.getTableId() != null) {
            Tables table = tablesRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));
            order.setTable(table);
        }

        if (request.getCustomerAccountId() != null) {
            Account customer = accountRepository.findById(request.getCustomerAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_ACCOUNT_NOT_FOUND));
            order.setCustomerAccount(customer);
        }

        if (request.getStaffAccountId() != null) {
            Account staff = accountRepository.findById(request.getStaffAccountId())
                    .orElseThrow(() -> new AppException(ErrorCode.STAFF_ACCOUNT_NOT_FOUND));
            order.setStaffAccount(staff);
        }

        if (request.getOrderType() != null && !request.getOrderType().isBlank()) {
            request.setOrderType(request.getOrderType().trim().toUpperCase());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            request.setStatus(request.getStatus().trim().toUpperCase());
        }

        orderMapper.updateOrder(request, order);

        BigDecimal subtotal = order.getSubtotal() != null ? order.getSubtotal() : BigDecimal.ZERO;

        if (request.getAppliedVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(request.getAppliedVoucherId())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
            BigDecimal discount = validateAndCalculateVoucherDiscount(voucher, subtotal);
            order.setAppliedVoucher(voucher);
            order.setDiscountAmount(discount);
        }

        BigDecimal effectiveDiscount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        order.setFinalAmount(subtotal.subtract(effectiveDiscount).max(BigDecimal.ZERO));

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        boolean hasPayments = order.getPayments() != null && !order.getPayments().isEmpty();
        boolean hasInvoice = order.getInvoice() != null;
        if (hasPayments || hasInvoice) {
            throw new AppException(ErrorCode.ORDER_HAS_PAYMENTS_OR_INVOICE);
        }

        orderRepository.delete(order);
    }

    private BigDecimal validateAndCalculateVoucherDiscount(Voucher voucher, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getUsageLimit() != null
                && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_UNAVAILABLE);
        }
        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (voucher.getDiscountValue() != null) {
            if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
                discount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (voucher.getMaxDiscountAmount() != null
                        && discount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                    discount = voucher.getMaxDiscountAmount();
                }
            } else {
                discount = voucher.getDiscountValue();
            }
        }
        return discount.min(subtotal);
    }

    private String generateUniqueOrderCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String candidate;
        do {
            String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            candidate = "ORD-" + datePart + "-" + randomPart;
        } while (orderRepository.existsByOrderCodeIgnoreCase(candidate));
        return candidate;
    }
}