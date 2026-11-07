package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.VoucherCreateRequest;
import com.example.cf_shop_management.dto.request.VoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.VoucherResponse;
import com.example.cf_shop_management.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {

    VoucherService voucherService;

    @PostMapping
    public ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody VoucherCreateRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.createVoucher(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VoucherResponse> getVoucherById(@PathVariable("id") Long id) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.getVoucherById(id))
                .build();
    }

    @GetMapping("/code/{code}")
    public ApiResponse<VoucherResponse> getVoucherByCode(@PathVariable("code") String code) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.getVoucherByCode(code))
                .build();
    }

    @GetMapping
    public ApiResponse<List<VoucherResponse>> getAllVouchers() {
        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherService.getAllVouchers())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<VoucherResponse> updateVoucher(
            @PathVariable("id") Long id,
            @Valid @RequestBody VoucherUpdateRequest request) {
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherService.updateVoucher(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteVoucher(@PathVariable("id") Long id) {
        voucherService.deleteVoucher(id);
        return ApiResponse.<String>builder()
                .result("Xóa voucher thành công")
                .build();
    }
}