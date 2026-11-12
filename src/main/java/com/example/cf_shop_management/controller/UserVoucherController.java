package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.UserVoucherCreateRequest;
import com.example.cf_shop_management.dto.request.UserVoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.UserVoucherResponse;
import com.example.cf_shop_management.service.UserVoucherService;
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
@RequestMapping("/api/v1/user-vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVoucherController {

    UserVoucherService userVoucherService;

    @PostMapping
    public ApiResponse<UserVoucherResponse> createUserVoucher(@Valid @RequestBody UserVoucherCreateRequest request) {
        return ApiResponse.<UserVoucherResponse>builder()
                .result(userVoucherService.createUserVoucher(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<UserVoucherResponse>> getAllUserVouchers(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long voucherId,
            @RequestParam(required = false) Boolean isUsed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<UserVoucherResponse>>builder()
                .result(userVoucherService.getAllUserVouchers(accountId, voucherId, isUsed, pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVoucherResponse> getUserVoucherById(@PathVariable Long id) {
        return ApiResponse.<UserVoucherResponse>builder()
                .result(userVoucherService.getUserVoucherById(id))
                .build();
    }

    @GetMapping("/account/{accountId}")
    public ApiResponse<List<UserVoucherResponse>> getUserVouchersByAccountId(@PathVariable Long accountId) {
        return ApiResponse.<List<UserVoucherResponse>>builder()
                .result(userVoucherService.getUserVouchersByAccountId(accountId))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVoucherResponse> updateUserVoucher(
            @PathVariable Long id,
            @Valid @RequestBody UserVoucherUpdateRequest request
    ) {
        return ApiResponse.<UserVoucherResponse>builder()
                .result(userVoucherService.updateUserVoucher(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUserVoucher(@PathVariable Long id) {
        userVoucherService.deleteUserVoucher(id);
        return ApiResponse.<String>builder()
                .result("Xóa voucher khỏi ví người dùng thành công")
                .build();
    }
}