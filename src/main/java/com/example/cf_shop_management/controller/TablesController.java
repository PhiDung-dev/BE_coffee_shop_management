package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.TablesCreateRequest;
import com.example.cf_shop_management.dto.request.TablesUpdateRequest;

import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.service.TablesService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TablesController {

    TablesService tablesService;

    @PostMapping
    public ApiResponse<TablesResponse> createTable(@Valid @RequestBody TablesCreateRequest request) {
        return ApiResponse.<TablesResponse>builder()
                .result(tablesService.createTable(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TablesResponse> getTableById(@PathVariable("id") Long id) {
        return ApiResponse.<TablesResponse>builder()
                .result(tablesService.getTableById(id))
                .build();
    }

    @GetMapping("/number/{tableNumber}")
    public ApiResponse<TablesResponse> getTableByNumber(@PathVariable("tableNumber") String tableNumber) {
        return ApiResponse.<TablesResponse>builder()
                .result(tablesService.getTableByNumber(tableNumber))
                .build();
    }

    @GetMapping
    public ApiResponse<List<TablesResponse>> getAllTables() {
        return ApiResponse.<List<TablesResponse>>builder()
                .result(tablesService.getAllTables())
                .build();
    }

    @GetMapping("/area/{areaId}")
    public ApiResponse<List<TablesResponse>> getTablesByArea(@PathVariable("areaId") Long areaId) {
        return ApiResponse.<List<TablesResponse>>builder()
                .result(tablesService.getTablesByArea(areaId))
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<TablesResponse>> getTablesByStatus(@PathVariable("status") String status) {
        return ApiResponse.<List<TablesResponse>>builder()
                .result(tablesService.getTablesByStatus(status))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TablesResponse> updateTable(
            @PathVariable("id") Long id,
            @Valid @RequestBody TablesUpdateRequest request) {
        return ApiResponse.<TablesResponse>builder()
                .result(tablesService.updateTable(id, request))
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<TablesResponse> updateTableStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {
        return ApiResponse.<TablesResponse>builder()
                .result(tablesService.updateTableStatus(id, status))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTable(@PathVariable("id") Long id) {
        tablesService.deleteTable(id);
        return ApiResponse.<String>builder()
                .result("Xóa bàn thành công")
                .build();
    }
}
