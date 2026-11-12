package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.OrderItemCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderItemResponse;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.OrderItem;
import com.example.cf_shop_management.entity.Product;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.OrderItemMapper;
import com.example.cf_shop_management.repository.OrderItemRepository;
import com.example.cf_shop_management.repository.OrderRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemService {

    OrderItemRepository orderItemRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;
    OrderItemMapper orderItemMapper;

    @Transactional
    public OrderItemResponse createOrderItem(OrderItemCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        validateOrderModifiable(order);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (Boolean.FALSE.equals(product.getIsAvailable())) {
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
        }

        OrderItem orderItem = orderItemMapper.toOrderItem(request);
        orderItem.setOrder(order);
        orderItem.setProduct(product);

        BigDecimal effectiveUnitPrice = (request.getUnitPrice() != null)
                ? request.getUnitPrice()
                : (product.getBasePrice() != null ? product.getBasePrice() : BigDecimal.ZERO);

        orderItem.setUnitPrice(effectiveUnitPrice);
        orderItem.setSubtotal(effectiveUnitPrice.multiply(BigDecimal.valueOf(request.getQuantity())));

        OrderItem savedItem = orderItemRepository.save(orderItem);
        order.getOrderItems().add(savedItem);
        recalculateOrderTotals(order);

        return orderItemMapper.toOrderItemResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public Page<OrderItemResponse> getAllOrderItems(Long orderId, Long productId, Pageable pageable) {
        return orderItemRepository.searchOrderItems(orderId, productId, pageable)
                .map(orderItemMapper::toOrderItemResponse);
    }

    @Transactional(readOnly = true)
    public OrderItemResponse getOrderItemById(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        return orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId)
                .stream()
                .map(orderItemMapper::toOrderItemResponse)
                .toList();
    }

    @Transactional
    public OrderItemResponse updateOrderItem(Long id, OrderItemUpdateRequest request) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        Order order = orderItem.getOrder();
        validateOrderModifiable(order);

        if (request.getProductId() != null && !request.getProductId().equals(orderItem.getProduct().getId())) {
            Product newProduct = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            if (Boolean.FALSE.equals(newProduct.getIsAvailable())) {
                throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
            }
            orderItem.setProduct(newProduct);

            if (request.getUnitPrice() == null && newProduct.getBasePrice() != null) {
                orderItem.setUnitPrice(newProduct.getBasePrice());
            }
        }

        orderItemMapper.updateOrderItem(orderItem, request);

        BigDecimal unitPrice = orderItem.getUnitPrice() != null ? orderItem.getUnitPrice() : BigDecimal.ZERO;
        orderItem.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(orderItem.getQuantity())));

        OrderItem updatedItem = orderItemRepository.save(orderItem);
        recalculateOrderTotals(order);

        return orderItemMapper.toOrderItemResponse(updatedItem);
    }

    @Transactional
    public void deleteOrderItem(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        Order order = orderItem.getOrder();
        validateOrderModifiable(order);

        order.getOrderItems().remove(orderItem);
        orderItemRepository.delete(orderItem);
        recalculateOrderTotals(order);
    }

    private void validateOrderModifiable(Order order) {
        if (order.getStatus() != null) {
            String status = order.getStatus().trim().toUpperCase();
            if ("COMPLETED".equals(status) || "CANCELLED".equals(status)) {
                throw new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED);
            }
        }
    }

    private void recalculateOrderTotals(Order order) {
        BigDecimal orderSubtotal = order.getOrderItems().stream()
                .map(item -> item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = orderSubtotal.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        order.setSubtotal(orderSubtotal);
        order.setFinalAmount(finalAmount);
        orderRepository.save(order);
    }
}