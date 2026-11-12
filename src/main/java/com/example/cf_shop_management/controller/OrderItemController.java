package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.OrderItemCreateRequest;
import com.example.cf_shop_management.dto.request.OrderItemUpdateRequest;
import com.example.cf_shop_management.dto.response.OrderItemResponse;
import com.example.cf_shop_management.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemController {

    OrderItemService orderItemService;

    @PostMapping
    public ApiResponse<OrderItemResponse> createOrderItem(@Valid @RequestBody OrderItemCreateRequest request) {
        return ApiResponse.<OrderItemResponse>builder()
                .result(orderItemService.createOrderItem(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<OrderItemResponse>> getAllOrderItems(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<OrderItemResponse>>builder()
                .result(orderItemService.getAllOrderItems(orderId, productId, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderItemResponse> getOrderItemById(@PathVariable Long id) {
        return ApiResponse.<OrderItemResponse>builder()
                .result(orderItemService.getOrderItemById(id))
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<List<OrderItemResponse>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        return ApiResponse.<List<OrderItemResponse>>builder()
                .result(orderItemService.getOrderItemsByOrderId(orderId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrderItemResponse> updateOrderItem(
            @PathVariable Long id,
            @Valid @RequestBody OrderItemUpdateRequest request
    ) {
        return ApiResponse.<OrderItemResponse>builder()
                .result(orderItemService.updateOrderItem(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return ApiResponse.<String>builder()
                .result("Xóa món khỏi đơn hàng thành công")
                .build();
    }
}