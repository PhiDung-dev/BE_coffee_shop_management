package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.ApiResponse;
import com.example.cf_shop_management.dto.request.LoginRequest;
import com.example.cf_shop_management.dto.response.AuthResponse;
import com.example.cf_shop_management.service.AuthService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.<AuthResponse>builder()
                .result(authService.login(request))
                .build();
    }
}