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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public ApiResponse<Page<AreaResponse>> getAllAreas(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<AreaResponse>>builder()
                .result(areaService.getAllAreas(keyword, pageable))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<AreaResponse>> getAllAreasList() {
        return ApiResponse.<List<AreaResponse>>builder()
                .result(areaService.getAllAreasList())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AreaResponse> getAreaById(@PathVariable Long id) {
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.getAreaById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AreaResponse> updateArea(
            @PathVariable Long id,
            @Valid @RequestBody AreaUpdateRequest request
    ) {
        return ApiResponse.<AreaResponse>builder()
                .result(areaService.updateArea(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteArea(@PathVariable Long id) {
        areaService.deleteArea(id);
        return ApiResponse.<String>builder()
                .result("Xóa khu vực thành công")
                .build();
    }
}