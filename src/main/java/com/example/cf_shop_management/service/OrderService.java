package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.OrderItem;
import com.example.cf_shop_management.entity.Product;
import com.example.cf_shop_management.entity.Voucher;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.OrderMapper;
import com.example.cf_shop_management.repository.AccountRepository;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import com.example.cf_shop_management.repository.VoucherRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    ProductRepository productRepository;
    TablesRepository tablesRepository;
    AccountRepository accountRepository;
    VoucherRepository voucherRepository;
    OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        Order order = orderMapper.toOrder(request);
        order.setOrderCode(generateOrderCode());
        order.setStatus("PENDING");

        bindRelations(order, request.getTableId(), request.getCustomerAccountId(), request.getStaffAccountId());

        Set<OrderItem> orderItems = buildOrderItems(order, request.getItems());
        order.setOrderItems(orderItems);

        calculateOrderAmounts(order, request.getVoucherCode());

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerAccountId) {
        return orderRepository.findByCustomerAccountIdOrderByCreatedAtDesc(customerAccountId)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        orderMapper.updateOrder(request, order);
        bindRelations(order, request.getTableId(), null, request.getStaffAccountId());

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            order.getOrderItems().clear();
            order.getOrderItems().addAll(buildOrderItems(order, request.getItems()));
        }

        String voucherCode = request.getVoucherCode() != null
                ? request.getVoucherCode()
                : (order.getAppliedVoucher() != null ? order.getAppliedVoucher().getCode() : null);

        calculateOrderAmounts(order, voucherCode);

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        order.setStatus(status);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        orderRepository.delete(order);
    }

    // --- Helper methods ---

    private void bindRelations(Order order, Long tableId, Long customerAccountId, Long staffAccountId) {
        if (tableId != null) {
            Tables table = tablesRepository.findById(tableId)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_EXISTED));
            order.setTable(table);
        }
        if (customerAccountId != null) {
            Account customer = accountRepository.findById(customerAccountId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            order.setCustomerAccount(customer);
        }
        if (staffAccountId != null) {
            Account staff = accountRepository.findById(staffAccountId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            order.setStaffAccount(staff);
        }
    }

    private Set<OrderItem> buildOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        Set<OrderItem> items = new HashSet<>();
        for (OrderItemRequest itemReq : itemRequests) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

            BigDecimal unitPrice = product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO;
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .selectedOptions(itemReq.getSelectedOptions())
                    .unitPrice(unitPrice)
                    .subtotal(itemSubtotal)
                    .build();
            items.add(orderItem);
        }
        return items;
    }

    private void calculateOrderAmounts(Order order, String voucherCode) {
        BigDecimal subtotal = order.getOrderItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (voucherCode != null && !voucherCode.isBlank()) {
            Voucher voucher = voucherRepository.findByCode(voucherCode)
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_EXISTED));

            validateVoucher(voucher, subtotal);

            if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
                discountAmount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                if (voucher.getMaxDiscountAmount() != null
                        && discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                    discountAmount = voucher.getMaxDiscountAmount();
                }
            } else {
                discountAmount = voucher.getDiscountValue() != null ? voucher.getDiscountValue() : BigDecimal.ZERO;
            }

            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal;
            }

            order.setAppliedVoucher(voucher);
        } else {
            order.setAppliedVoucher(null);
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(subtotal.subtract(discountAmount));
    }

    private void validateVoucher(Voucher voucher, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
        if (voucher.getMinOrderValue() != null && subtotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_INVALID);
        }
    }

    private String generateOrderCode() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }
}
