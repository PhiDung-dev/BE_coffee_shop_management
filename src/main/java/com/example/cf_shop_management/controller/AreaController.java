package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.AreaCreateRequest;
import com.example.cf_shop_management.dto.request.AreaUpdateRequest;

import com.example.cf_shop_management.dto.response.AreaResponse;
import com.example.cf_shop_management.service.AreaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AreaController {

    AreaService areaService;

    @PostMapping
    public ApiResponse<AreaResponse> createArea(@Valid @RequestBody AreaCreateRequest request) {
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.createArea(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AreaResponse> getAreaById(@PathVariable("id") Long id) {
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.getAreaById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<AreaResponse>> getAllAreas() {
        return ApiResponse.<List<AreaResponse>>builder()
                .result(areaService.getAllAreas())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AreaResponse> updateArea(
            @PathVariable("id") Long id,
            @Valid @RequestBody AreaUpdateRequest request) {
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.updateArea(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteArea(@PathVariable("id") Long id) {
        areaService.deleteArea(id);
        return ApiResponse.<String>builder()
                .result("Xóa khu vực thành công")
                .build();
    }
}
