package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.ForgotPasswordRequest;
import com.example.cf_shop_management.dto.request.ResetPasswordRequest;
import com.example.cf_shop_management.dto.response.PasswordResetResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.PasswordResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @Test
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("user@cfshop.vn")
                .build();

        PasswordResetResponse response = PasswordResetResponse.builder()
                .id(1L)
                .accountId(5L)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordResetService.createResetToken(any(ForgotPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạo mã token đặt lại mật khẩu thành công"))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.accountId").value(5L))
                .andExpect(jsonPath("$.result.isUsed").value(false));
    }

    @Test
    void forgotPassword_AccountNotFound_Returns4xx() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("notfound@cfshop.vn")
                .build();

        when(passwordResetService.createResetToken(any(ForgotPasswordRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("reset-token-uuid-123")
                .newPassword("NewPass@123")
                .build();

        doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."));

        verify(passwordResetService, times(1)).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPassword_TokenExpired_Returns4xx() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("expired-token-123")
                .newPassword("NewPass@123")
                .build();

        doThrow(new AppException(ErrorCode.TOKEN_EXPIRED))
                .when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void resetPassword_TokenAlreadyUsed_Returns4xx() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("used-token-123")
                .newPassword("NewPass@123")
                .build();

        doThrow(new AppException(ErrorCode.TOKEN_ALREADY_USED))
                .when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }
}