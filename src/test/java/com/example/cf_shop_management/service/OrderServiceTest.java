package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.entity.Account;
import com.example.cf_shop_management.entity.Invoice;
import com.example.cf_shop_management.entity.Order;
import com.example.cf_shop_management.entity.OrderItem;
import com.example.cf_shop_management.entity.Payment;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TablesRepository tablesRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    private Tables table;
    private Account customerAccount;
    private Account staffAccount;
    private Product product;
    private Voucher percentageVoucher;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        table = Tables.builder()
                .id(2L)
                .tableNumber("B02")
                .status("AVAILABLE")
                .build();

        customerAccount = Account.builder()
                .id(5L)
                .email("customer@gmail.com")
                .build();

        staffAccount = Account.builder()
                .id(2L)
                .email("staff@cfshop.vn")
                .build();

        product = Product.builder()
                .id(3L)
                .name("Cà phê sữa đá")
                .basePrice(new BigDecimal("40000.00"))
                .isAvailable(true)
                .build();

        percentageVoucher = Voucher.builder()
                .id(1L)
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .minOrderValue(new BigDecimal("50000.00"))
                .maxDiscountAmount(new BigDecimal("15000.00"))
                .usageLimit(100)
                .usedCount(5)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .isActive(true)
                .build();

        order = Order.builder()
                .id(1L)
                .orderCode("ORD-20261116-8899AA")
                .orderType("DINE_IN")
                .status("PENDING")
                .subtotal(new BigDecimal("80000.00"))
                .discountAmount(new BigDecimal("8000.00"))
                .finalAmount(new BigDecimal("72000.00"))
                .orderItems(new HashSet<>())
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-20261116-8899AA")
                .orderType("DINE_IN")
                .status("PENDING")
                .subtotal(new BigDecimal("80000.00"))
                .discountAmount(new BigDecimal("8000.00"))
                .finalAmount(new BigDecimal("72000.00"))
                .build();
    }

    @Test
    void createOrder_WithItemsAndVoucher_CalculatesTotalsAndIncrementsVoucher_Success() {
        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(3L)
                .quantity(2)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("  dine_in  ")
                .tableId(2L)
                .customerAccountId(5L)
                .staffAccountId(2L)
                .appliedVoucherId(1L)
                .items(List.of(itemReq))
                .build();

        Order mappedOrder = Order.builder()
                .orderItems(new HashSet<>())
                .build();
        OrderItem mappedItem = new OrderItem();

        when(orderMapper.toOrder(request)).thenReturn(mappedOrder);
        when(orderRepository.existsByOrderCodeIgnoreCase(anyString())).thenReturn(false);
        when(tablesRepository.findById(2L)).thenReturn(Optional.of(table));
        when(accountRepository.findById(5L)).thenReturn(Optional.of(customerAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(staffAccount));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(orderItemMapper.toOrderItem(itemReq)).thenReturn(mappedItem);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(percentageVoucher));
        when(orderRepository.save(mappedOrder)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals("DINE_IN", mappedOrder.getOrderType());
        assertEquals("PENDING", mappedOrder.getStatus());
        assertEquals(new BigDecimal("80000.00"), mappedOrder.getSubtotal());
        assertEquals(new BigDecimal("8000.00"), mappedOrder.getDiscountAmount());
        assertEquals(new BigDecimal("72000.00"), mappedOrder.getFinalAmount());
        assertEquals(6, percentageVoucher.getUsedCount());
        verify(voucherRepository).save(percentageVoucher);
        verify(orderRepository).save(mappedOrder);
    }

    @Test
    void createOrder_TableNotAvailable_ThrowsException() {
        Tables maintenanceTable = Tables.builder()
                .id(2L)
                .status("MAINTENANCE")
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("DINE_IN")
                .tableId(2L)
                .build();

        when(orderMapper.toOrder(request)).thenReturn(Order.builder().orderItems(new HashSet<>()).build());
        when(orderRepository.existsByOrderCodeIgnoreCase(anyString())).thenReturn(false);
        when(tablesRepository.findById(2L)).thenReturn(Optional.of(maintenanceTable));

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(request));

        assertEquals(ErrorCode.TABLE_NOT_AVAILABLE, exception.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_VoucherMinOrderNotMet_ThrowsException() {
        OrderItemRequest itemReq = OrderItemRequest.builder()
                .productId(3L)
                .quantity(1) // 40.000 < minOrderValue 50.000
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("TAKEAWAY")
                .appliedVoucherId(1L)
                .items(List.of(itemReq))
                .build();

        Order mappedOrder = Order.builder().orderItems(new HashSet<>()).build();

        when(orderMapper.toOrder(request)).thenReturn(mappedOrder);
        when(orderRepository.existsByOrderCodeIgnoreCase(anyString())).thenReturn(false);
        when(productRepository.findById(3L)).thenReturn(Optional.of(product));
        when(orderItemMapper.toOrderItem(itemReq)).thenReturn(new OrderItem());
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(percentageVoucher));

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(request));

        assertEquals(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET, exception.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_VoucherUnavailable_ThrowsException() {
        Voucher inactiveVoucher = Voucher.builder()
                .id(1L)
                .isActive(false)
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("TAKEAWAY")
                .appliedVoucherId(1L)
                .build();

        when(orderMapper.toOrder(request)).thenReturn(Order.builder().orderItems(new HashSet<>()).build());
        when(orderRepository.existsByOrderCodeIgnoreCase(anyString())).thenReturn(false);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(inactiveVoucher));

        AppException exception = assertThrows(AppException.class,
                () -> orderService.createOrder(request));

        assertEquals(ErrorCode.VOUCHER_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void getAllOrders_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.searchOrders(eq("PENDING"), eq("DINE_IN"), eq(2L), isNull(), isNull(),
                isNull(), isNull(), eq("ORD-2026"), eq(pageable))).thenReturn(page);
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        Page<OrderResponse> result = orderService.getAllOrders(
                "PENDING", "DINE_IN", 2L, null, null, null, null, "  ORD-2026  ", pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> orderService.getOrderById(99L));

        assertEquals(ErrorCode.ORDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getOrderByCode_Success() {
        when(orderRepository.findByOrderCodeIgnoreCase("ORD-20261116-8899AA")).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderByCode("  ORD-20261116-8899AA  ");

        assertEquals("ORD-20261116-8899AA", result.getOrderCode());
    }

    @Test
    void updateOrder_Success() {
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .status("  preparing  ")
                .appliedVoucherId(1L)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(percentageVoucher));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateOrder(1L, request);

        assertNotNull(result);
        assertEquals("PREPARING", request.getStatus());
        assertEquals(new BigDecimal("8000.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("72000.00"), order.getFinalAmount());
        verify(orderMapper).updateOrder(request, order);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_CompletedStatus_ThrowsException() {
        order.setStatus("COMPLETED");
        OrderUpdateRequest request = OrderUpdateRequest.builder().status("CANCELLED").build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(AppException.class,
                () -> orderService.updateOrder(1L, request));

        assertEquals(ErrorCode.ORDER_CANNOT_BE_MODIFIED, exception.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void deleteOrder_HasPaymentsOrInvoice_ThrowsException() {
        order.setPayments(Set.of(Payment.builder().id(1L).build()));
        order.setInvoice(Invoice.builder().id(1L).build());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(AppException.class,
                () -> orderService.deleteOrder(1L));

        assertEquals(ErrorCode.ORDER_HAS_PAYMENTS_OR_INVOICE, exception.getErrorCode());
        verify(orderRepository, never()).delete(any());
    }
}