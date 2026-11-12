package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.OrderCreateRequest;
import com.example.cf_shop_management.dto.request.OrderUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderResponse;
import com.example.cf_shop_management.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrder(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable("id") Long id) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(id))
                .build();
    }

    @GetMapping("/code/{orderCode}")
    public ApiResponse<OrderResponse> getOrderByCode(@PathVariable("orderCode") String orderCode) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderByCode(orderCode))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getAllOrders())
                .build();
    }

    @GetMapping("/customer/{customerAccountId}")
    public ApiResponse<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable("customerAccountId") Long customerAccountId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getOrdersByCustomer(customerAccountId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderResponse> updateOrder(
            @PathVariable("id") Long id,
            @Valid @RequestBody OrderUpdateRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrder(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.updateOrderStatus(id, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrder(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
        return ApiResponse.<String>builder()
                .result("Xóa đơn hàng thành công")
                .build();
    }
}