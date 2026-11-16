package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.ForgotPasswordRequest;
import com.example.cf_shop_management.dto.request.ResetPasswordRequest;
import com.example.cf_shop_management.dto.response.PasswordResetResponse;
import com.example.cf_shop_management.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetController {

    PasswordResetService passwordResetService;

    // 1. Gửi yêu cầu quên mật khẩu -> Sinh Token
    @PostMapping("/forgot-password")
    public ApiResponse<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.<PasswordResetResponse>builder()
                .message("Tạo mã token đặt lại mật khẩu thành công")
                .result(passwordResetService.createResetToken(request))
                .build();
    }

    // 2. Xác nhận token và đổi mật khẩu mới
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.<String>builder()
                .result("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.")
                .build();
    }
}