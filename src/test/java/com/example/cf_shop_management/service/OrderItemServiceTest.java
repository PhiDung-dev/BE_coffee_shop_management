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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderItemService orderItemService;

    private Order pendingOrder;
    private Product product;
    private OrderItem orderItem;
    private OrderItemResponse orderItemResponse;

    @BeforeEach
    void setUp() {
        pendingOrder = Order.builder()
                .id(10L)
                .orderCode("ORD-20261116-8899AA")
                .status("PENDING")
                .subtotal(BigDecimal.ZERO)
                .discountAmount(new BigDecimal("10000.00"))
                .finalAmount(BigDecimal.ZERO)
                .orderItems(new HashSet<>())
                .build();

        product = Product.builder()
                .id(3L)
                .name("Cà phê sữa đá")
                .basePrice(new BigDecimal("35000.00"))
                .isAvailable(true)
                .build();

        orderItem = OrderItem.builder()
                .id(1L)
                .order(pendingOrder)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("35000.00"))
                .subtotal(new BigDecimal("70000.00"))
                .build();

        orderItemResponse = OrderItemResponse.builder()
                .id(1L)
                .orderId(10L)
                .productId(3L)
                .productName("Cà phê sữa đá")
                .quantity(2)
                .unitPrice(new BigDecimal("35000.00"))
                .subtotal(new BigDecimal("70000.00"))
                .build();
    }

    @Test
    void createOrderItem_Success_RecalculatesOrderTotals() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(3L)
                .quantity(2)
                .selectedOptions("{\"ice\":\"70%\"}")
                .build();

        OrderItem mappedItem = new OrderItem();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(pendingOrder));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(orderItemMapper.toOrderItem(request)).thenReturn(mappedItem);
        when(orderItemRepository.save(mappedItem)).thenReturn(orderItem);
        when(orderRepository.save(pendingOrder)).thenReturn(pendingOrder);
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        OrderItemResponse result = orderItemService.createOrderItem(request);

        assertNotNull(result);
        assertEquals(pendingOrder, mappedItem.getOrder());
        assertEquals(product, mappedItem.getProduct());
        assertEquals(new BigDecimal("35000.00"), mappedItem.getUnitPrice());
        assertEquals(new BigDecimal("70000.00"), mappedItem.getSubtotal());
        assertEquals(1, pendingOrder.getOrderItems().size());
        assertEquals(new BigDecimal("70000.00"), pendingOrder.getSubtotal());
        assertEquals(new BigDecimal("60000.00"), pendingOrder.getFinalAmount());
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void createOrderItem_OrderNotFound_ThrowsException() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(99L)
                .productId(3L)
                .quantity(1)
                .build();

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.createOrderItem(request));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void createOrderItem_OrderCompleted_ThrowsException() {
        Order completedOrder = Order.builder()
                .id(10L)
                .status("COMPLETED")
                .build();

        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(3L)
                .quantity(1)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(completedOrder));

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.createOrderItem(request));

        assertEquals(ErrorCode.ORDER_CANNOT_BE_MODIFIED, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void createOrderItem_ProductNotFound_ThrowsException() {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(99L)
                .quantity(1)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(pendingOrder));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.createOrderItem(request));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createOrderItem_ProductUnavailable_ThrowsException() {
        Product unavailableProduct = Product.builder()
                .id(3L)
                .name("Cà phê sữa đá")
                .basePrice(new BigDecimal("35000.00"))
                .isAvailable(false)
                .build();

        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(3L)
                .quantity(1)
                .build();

        when(orderRepository.findById(10L)).thenReturn(Optional.of(pendingOrder));
        when(productRepository.findById(3L)).thenReturn(Optional.of(unavailableProduct));

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.createOrderItem(request));

        assertEquals(ErrorCode.PRODUCT_UNAVAILABLE, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    void getAllOrderItems_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> page = new PageImpl<>(List.of(orderItem), pageable, 1);

        when(orderItemRepository.searchOrderItems(10L, 3L, pageable)).thenReturn(page);
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        Page<OrderItemResponse> result = orderItemService.getAllOrderItems(10L, 3L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderItemById_Success() {
        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        OrderItemResponse result = orderItemService.getOrderItemById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderItemById_NotFound_ThrowsException() {
        when(orderItemRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.getOrderItemById(99L));

        assertEquals(ErrorCode.ORDER_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getOrderItemsByOrderId_Success() {
        when(orderRepository.existsById(10L)).thenReturn(true);
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(orderItem));
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        List<OrderItemResponse> result = orderItemService.getOrderItemsByOrderId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getOrderId());
    }

    @Test
    void getOrderItemsByOrderId_OrderNotFound_ThrowsException() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> orderItemService.getOrderItemsByOrderId(99L));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateOrderItem_ChangeProductAndQuantity_Success() {
        pendingOrder.getOrderItems().add(orderItem);

        Product newProduct = Product.builder()
                .id(4L)
                .name("Bạc xỉu")
                .basePrice(new BigDecimal("40000.00"))
                .isAvailable(true)
                .build();

        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder()
                .productId(4L)
                .quantity(3)
                .build();

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(4L)).thenReturn(Optional.of(newProduct));
        doAnswer(inv -> {
            orderItem.setQuantity(3);
            return null;
        }).when(orderItemMapper).updateOrderItem(request, orderItem);
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);
        when(orderRepository.save(pendingOrder)).thenReturn(pendingOrder);
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        OrderItemResponse result = orderItemService.updateOrderItem(1L, request);

        assertNotNull(result);
        assertEquals(newProduct, orderItem.getProduct());
        assertEquals(new BigDecimal("40000.00"), orderItem.getUnitPrice());
        assertEquals(new BigDecimal("120000.00"), orderItem.getSubtotal());
        assertEquals(new BigDecimal("120000.00"), pendingOrder.getSubtotal());
        assertEquals(new BigDecimal("110000.00"), pendingOrder.getFinalAmount());
    }

    @Test
    void deleteOrderItem_Success_RecalculatesOrderTotals() {
        pendingOrder.getOrderItems().add(orderItem);

        when(orderItemRepository.findById(1L)).thenReturn(Optional.of(orderItem));
        doNothing().when(orderItemRepository).delete(orderItem);
        when(orderRepository.save(pendingOrder)).thenReturn(pendingOrder);

        orderItemService.deleteOrderItem(1L);

        assertTrue(pendingOrder.getOrderItems().isEmpty());
        assertEquals(BigDecimal.ZERO, pendingOrder.getSubtotal());
        assertEquals(BigDecimal.ZERO, pendingOrder.getFinalAmount());
        verify(orderItemRepository).delete(orderItem);
        verify(orderRepository).save(pendingOrder);
    }
}