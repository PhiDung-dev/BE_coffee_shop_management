package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.OrderItemCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderItemResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.OrderItemService;
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

@WebMvcTest(OrderItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderItemService orderItemService;

    private OrderItemResponse orderItemResponse;

    @BeforeEach
    void setUp() {
        orderItemResponse = OrderItemResponse.builder()
                .id(1L)
                .orderId(10L)
                .orderCode("ORD-20261116-8899AA")
                .productId(3L)
                .productName("Cà phê sữa đá")
                .productImageUrl("https://cdn.cfshop.vn/products/cfsd.png")
                .quantity(2)
                .selectedOptions("{\"ice\":\"70%\",\"sugar\":\"50%\"}")
                .unitPrice(new BigDecimal("35000.00"))
                .subtotal(new BigDecimal("70000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrderItem_Success() throws Exception {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(3L)
                .quantity(2)
                .selectedOptions("{\"ice\":\"70%\",\"sugar\":\"50%\"}")
                .build();

        when(orderItemService.createOrderItem(any(OrderItemCreateRequest.class))).thenReturn(orderItemResponse);

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.orderId").value(10L))
                .andExpect(jsonPath("$.result.productName").value("Cà phê sữa đá"))
                .andExpect(jsonPath("$.result.subtotal").value(70000.00));
    }

    @Test
    void createOrderItem_OrderCompleted_Returns4xx() throws Exception {
        OrderItemCreateRequest request = OrderItemCreateRequest.builder()
                .orderId(10L)
                .productId(3L)
                .quantity(1)
                .build();

        when(orderItemService.createOrderItem(any(OrderItemCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.ORDER_CANNOT_BE_MODIFIED));

        mockMvc.perform(post("/api/order-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllOrderItems_Paged_Success() throws Exception {
        Page<OrderItemResponse> pageResult = new PageImpl<>(
                List.of(orderItemResponse),
                PageRequest.of(0, 10),
                1
        );

        when(orderItemService.getAllOrderItems(eq(10L), eq(3L), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/order-items")
                        .param("orderId", "10")
                        .param("productId", "3")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].productName").value("Cà phê sữa đá"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getOrderItemById_Success() throws Exception {
        when(orderItemService.getOrderItemById(1L)).thenReturn(orderItemResponse);

        mockMvc.perform(get("/api/order-items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.quantity").value(2));
    }

    @Test
    void getOrderItemById_NotFound_Returns4xx() throws Exception {
        when(orderItemService.getOrderItemById(99L))
                .thenThrow(new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        mockMvc.perform(get("/api/order-items/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getOrderItemsByOrderId_Success() throws Exception {
        when(orderItemService.getOrderItemsByOrderId(10L)).thenReturn(List.of(orderItemResponse));

        mockMvc.perform(get("/api/order-items/order/{orderId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].orderCode").value("ORD-20261116-8899AA"));
    }

    @Test
    void updateOrderItem_Success() throws Exception {
        OrderItemUpdateRequest request = OrderItemUpdateRequest.builder()
                .quantity(3)
                .selectedOptions("{\"ice\":\"100%\",\"sugar\":\"70%\"}")
                .build();

        OrderItemResponse updated = OrderItemResponse.builder()
                .id(1L)
                .orderId(10L)
                .productId(3L)
                .productName("Cà phê sữa đá")
                .quantity(3)
                .unitPrice(new BigDecimal("35000.00"))
                .subtotal(new BigDecimal("105000.00"))
                .build();

        when(orderItemService.updateOrderItem(eq(1L), any(OrderItemUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/order-items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.quantity").value(3))
                .andExpect(jsonPath("$.result.subtotal").value(105000.00));
    }

    @Test
    void deleteOrderItem_Success() throws Exception {
        doNothing().when(orderItemService).deleteOrderItem(1L);

        mockMvc.perform(delete("/api/order-items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa món khỏi đơn hàng thành công"));

        verify(orderItemService, times(1)).deleteOrderItem(1L);
    }
}