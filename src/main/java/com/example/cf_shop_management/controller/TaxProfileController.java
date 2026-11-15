package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.TaxProfileCreateRequest;
import com.example.cf_shop_management.dto.request.TaxProfileUpdateRequest;
import com.example.cf_shop_management.dto.response.TaxProfileResponse;
import com.example.cf_shop_management.service.TaxProfileService;
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
@RequestMapping("/api/v1/tax-profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaxProfileController {

    TaxProfileService taxProfileService;

    @PostMapping
    public ApiResponse<TaxProfileResponse> createTaxProfile(@Valid @RequestBody TaxProfileCreateRequest request) {
        return ApiResponse.<TaxProfileResponse>builder()
                .result(taxProfileService.createTaxProfile(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<TaxProfileResponse>> getAllTaxProfiles(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<TaxProfileResponse>>builder()
                .result(taxProfileService.getAllTaxProfiles(accountId, keyword, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<TaxProfileResponse> getTaxProfileById(@PathVariable Long id) {
        return ApiResponse.<TaxProfileResponse>builder()
                .result(taxProfileService.getTaxProfileById(id))
                .build();
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<TaxProfileResponse>> getTaxProfilesByAccountId(@PathVariable Long accountId) {
        return ApiResponse.<List<TaxProfileResponse>>builder()
                .result(taxProfileService.getTaxProfilesByAccountId(accountId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<TaxProfileResponse> updateTaxProfile(
            @PathVariable Long id,
            @Valid @RequestBody TaxProfileUpdateRequest request
    ) {
        return ApiResponse.<TaxProfileResponse>builder()
                .result(taxProfileService.updateTaxProfile(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTaxProfile(@PathVariable Long id) {
        taxProfileService.deleteTaxProfile(id);
        return ApiResponse.<String>builder()
                .result("Xóa thông tin xuất hóa đơn thành công")
                .build();
    }
}