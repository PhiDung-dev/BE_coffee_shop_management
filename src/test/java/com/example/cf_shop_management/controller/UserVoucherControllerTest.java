package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.UserVoucherCreateRequest;
import com.example.cf_shop_management.dto.request.UserVoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.UserVoucherResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.UserVoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserVoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserVoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserVoucherService userVoucherService;

    private UserVoucherResponse unusedUserVoucher;
    private UserVoucherResponse usedUserVoucher;

    @BeforeEach
    void setUp() {
        unusedUserVoucher = UserVoucherResponse.builder()
                .id(1L)
                .accountId(5L)
                .voucherId(2L)
                .isUsed(false)
                .usedAt(null)
                .build();

        usedUserVoucher = UserVoucherResponse.builder()
                .id(1L)
                .accountId(5L)
                .voucherId(2L)
                .isUsed(true)
                .usedAt(LocalDateTime.of(2026, 11, 16, 15, 0))
                .build();
    }

    @Test
    void createUserVoucher_Success() throws Exception {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(userVoucherService.createUserVoucher(any(UserVoucherCreateRequest.class))).thenReturn(unusedUserVoucher);

        mockMvc.perform(post("/api/user-vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.accountId").value(5L))
                .andExpect(jsonPath("$.result.voucherId").value(2L))
                .andExpect(jsonPath("$.result.isUsed").value(false));
    }

    @Test
    void createUserVoucher_AlreadyExisted_Returns4xx() throws Exception {
        UserVoucherCreateRequest request = UserVoucherCreateRequest.builder()
                .accountId(5L)
                .voucherId(2L)
                .build();

        when(userVoucherService.createUserVoucher(any(UserVoucherCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.USER_VOUCHER_EXISTED));

        mockMvc.perform(post("/api/user-vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllUserVouchers_Paged_Success() throws Exception {
        Page<UserVoucherResponse> pageResult = new PageImpl<>(
                List.of(unusedUserVoucher),
                PageRequest.of(0, 10),
                1
        );

        when(userVoucherService.getAllUserVouchers(eq(5L), eq(2L), eq(false), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/user-vouchers")
                        .param("accountId", "5")
                        .param("voucherId", "2")
                        .param("isUsed", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].isUsed").value(false))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getUserVoucherById_Success() throws Exception {
        when(userVoucherService.getUserVoucherById(1L)).thenReturn(unusedUserVoucher);

        mockMvc.perform(get("/api/user-vouchers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.voucherId").value(2L));
    }

    @Test
    void getUserVoucherById_NotFound_Returns4xx() throws Exception {
        when(userVoucherService.getUserVoucherById(99L))
                .thenThrow(new AppException(ErrorCode.USER_VOUCHER_NOT_FOUND));

        mockMvc.perform(get("/api/user-vouchers/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getUserVouchersByAccountId_Success() throws Exception {
        when(userVoucherService.getUserVouchersByAccountId(5L)).thenReturn(List.of(unusedUserVoucher));

        mockMvc.perform(get("/api/user-vouchers/account/{accountId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].accountId").value(5L));
    }

    @Test
    void updateUserVoucher_Success() throws Exception {
        UserVoucherUpdateRequest request = UserVoucherUpdateRequest.builder()
                .isUsed(true)
                .build();

        when(userVoucherService.updateUserVoucher(eq(1L), any(UserVoucherUpdateRequest.class))).thenReturn(usedUserVoucher);

        mockMvc.perform(put("/api/user-vouchers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isUsed").value(true));
    }

    @Test
    void deleteUserVoucher_Success() throws Exception {
        doNothing().when(userVoucherService).deleteUserVoucher(1L);

        mockMvc.perform(delete("/api/user-vouchers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa voucher khỏi ví người dùng thành công"));

        verify(userVoucherService, times(1)).deleteUserVoucher(1L);
    }

    @Test
    void deleteUserVoucher_AlreadyUsed_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.USER_VOUCHER_ALREADY_USED)).when(userVoucherService).deleteUserVoucher(1L);

        mockMvc.perform(delete("/api/user-vouchers/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}