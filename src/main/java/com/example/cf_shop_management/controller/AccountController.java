package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.AccountCreateRequest;
import com.example.cf_shop_management.dto.request.AccountUpdateRequest;
import com.example.cf_shop_management.dto.response.AccountResponse;
import com.example.cf_shop_management.service.AccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    AccountService accountService;

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.createAccount(request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable("id") Long id) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.getAccountById(id))
                .build();
    }

    @GetMapping
    public ApiResponse<List<AccountResponse>> getAllAccounts() {
        return ApiResponse.<List<AccountResponse>>builder()
                .result(accountService.getAllAccounts())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> updateAccount(@PathVariable("id") Long id, @Valid @RequestBody AccountUpdateRequest request) {
        return ApiResponse.<AccountResponse>builder()
                .result(accountService.updateAccount(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAccount(@PathVariable("id") Long id) {
        accountService.deleteAccount(id);
        return ApiResponse.<String>builder()
                .result("Xóa tài khoản thành công")
                .build();
    }
}