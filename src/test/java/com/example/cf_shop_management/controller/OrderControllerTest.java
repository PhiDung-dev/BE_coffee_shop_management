package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderItemResponse;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.OrderService;
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

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .id(100L)
                .orderId(1L)
                .orderCode("ORD-20261116-8899AA")
                .productId(3L)
                .productName("Cà phê sữa đá")
                .quantity(2)
                .selectedOptions("{\"ice\":\"70%\",\"sugar\":\"50%\"}")
                .unitPrice(new BigDecimal("35000.00"))
                .subtotal(new BigDecimal("70000.00"))
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-20261116-8899AA")
                .orderType("DINE_IN")
                .status("PENDING")
                .subtotal(new BigDecimal("70000.00"))
                .discountAmount(new BigDecimal("10000.00"))
                .finalAmount(new BigDecimal("60000.00"))
                .tableId(2L)
                .tableNumber("B02")
                .customerAccountId(5L)
                .customerEmail("customer@gmail.com")
                .staffAccountId(2L)
                .staffEmail("staff@cfshop.vn")
                .appliedVoucherId(1L)
                .appliedVoucherCode("WELCOME10")
                .orderItems(List.of(itemResponse))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_Success() throws Exception {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(3L)
                .quantity(2)
                .selectedOptions("{\"ice\":\"70%\",\"sugar\":\"50%\"}")
                .build();

        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("DINE_IN")
                .status("PENDING")
                .tableId(2L)
                .customerAccountId(5L)
                .staffAccountId(2L)
                .appliedVoucherId(1L)
                .items(List.of(itemRequest))
                .build();

        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.orderCode").value("ORD-20261116-8899AA"))
                .andExpect(jsonPath("$.result.finalAmount").value(60000.00))
                .andExpect(jsonPath("$.result.orderItems[0].productName").value("Cà phê sữa đá"));
    }

    @Test
    void createOrder_VoucherMinOrderNotMet_Returns4xx() throws Exception {
        OrderCreateRequest request = OrderCreateRequest.builder()
                .orderType("TAKEAWAY")
                .appliedVoucherId(1L)
                .items(List.of(OrderItemRequest.builder().productId(3L).quantity(1).build()))
                .build();

        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllOrders_Paged_Success() throws Exception {
        Page<OrderResponse> pageResult = new PageImpl<>(
                List.of(orderResponse),
                PageRequest.of(0, 10),
                1
        );

        when(orderService.getAllOrders(
                eq("PENDING"),
                eq("DINE_IN"),
                eq(2L),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq("ORD-2026"),
                any(Pageable.class)
        )).thenReturn(pageResult);

        mockMvc.perform(get("/api/orders")
                        .param("status", "PENDING")
                        .param("orderType", "DINE_IN")
                        .param("tableId", "2")
                        .param("keyword", "ORD-2026")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].orderCode").value("ORD-20261116-8899AA"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getOrderById_Success() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.tableNumber").value("B02"));
    }

    @Test
    void getOrderByCode_Success() throws Exception {
        when(orderService.getOrderByCode("ORD-20261116-8899AA")).thenReturn(orderResponse);

        mockMvc.perform(get("/api/orders/code/{orderCode}", "ORD-20261116-8899AA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderCode").value("ORD-20261116-8899AA"));
    }

    @Test
    void updateOrder_Success() throws Exception {
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .status("PREPARING")
                .tableId(4L)
                .build();

        OrderResponse updated = OrderResponse.builder()
                .id(1L)
                .orderCode("ORD-20261116-8899AA")
                .status("PREPARING")
                .tableId(4L)
                .tableNumber("B04")
                .build();

        when(orderService.updateOrder(eq(1L), any(OrderUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("PREPARING"))
                .andExpect(jsonPath("$.result.tableNumber").value("B04"));
    }

    @Test
    void updateOrder_CompletedCannotModify_Returns4xx() throws Exception {
        OrderUpdateRequest request = OrderUpdateRequest.builder()
                .status("CANCELLED")
                .build();

        when(orderService.updateOrder(eq(1L), any(OrderUpdateRequest.class)))
                .thenThrow(new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED));

        mockMvc.perform(put("/api/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa đơn hàng thành công"));

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void deleteOrder_HasPaymentsOrInvoice_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.ORDER_HAS_PAYMENTS_OR_INVOICE)).when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}