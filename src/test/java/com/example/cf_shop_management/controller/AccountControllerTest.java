package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.AccountCreateRequest;
import com.example.cf_shop_management.dto.request.AccountUpdateRequest;
import com.example.cf_shop_management.dto.response.AccountResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    private AccountResponse accountResponse;

    @BeforeEach
    void setUp() {
        accountResponse = AccountResponse.builder()
                .id(1L)
                .email("staff@cfshop.vn")
                .phone("0905123456")
                .status("ACTIVE")
                .roleName("STAFF")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAccount_Success() throws Exception {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("staff@cfshop.vn")
                .password("123456")
                .phone("0905123456")
                .status("ACTIVE")
                .roleId(2L)
                .build();

        when(accountService.createAccount(any(AccountCreateRequest.class))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.email").value("staff@cfshop.vn"))
                .andExpect(jsonPath("$.result.roleName").value("STAFF"));
    }

    @Test
    void createAccount_EmailExisted_Returns4xx() throws Exception {
        AccountCreateRequest request = AccountCreateRequest.builder()
                .email("staff@cfshop.vn")
                .password("123456")
                .roleId(2L)
                .build();

        when(accountService.createAccount(any(AccountCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.EMAIL_EXISTED));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAccountById_Success() throws Exception {
        when(accountService.getAccountById(1L)).thenReturn(accountResponse);

        mockMvc.perform(get("/api/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.email").value("staff@cfshop.vn"));
    }

    @Test
    void getAccountById_NotFound_Returns4xx() throws Exception {
        when(accountService.getAccountById(99L))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        mockMvc.perform(get("/api/accounts/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllAccounts_Success() throws Exception {
        AccountResponse secondAccount = AccountResponse.builder()
                .id(2L)
                .email("admin@cfshop.vn")
                .status("ACTIVE")
                .roleName("ADMIN")
                .build();

        when(accountService.getAllAccounts()).thenReturn(List.of(accountResponse, secondAccount));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[1].email").value("admin@cfshop.vn"));
    }

    @Test
    void updateAccount_Success() throws Exception {
        AccountUpdateRequest request = AccountUpdateRequest.builder()
                .phone("0911222333")
                .status("INACTIVE")
                .build();

        AccountResponse updatedResponse = AccountResponse.builder()
                .id(1L)
                .email("staff@cfshop.vn")
                .phone("0911222333")
                .status("INACTIVE")
                .roleName("STAFF")
                .build();

        when(accountService.updateAccount(eq(1L), any(AccountUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/accounts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.phone").value("0911222333"))
                .andExpect(jsonPath("$.result.status").value("INACTIVE"));
    }

    @Test
    void deleteAccount_Success() throws Exception {
        doNothing().when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa tài khoản thành công"));

        verify(accountService, times(1)).deleteAccount(1L);
    }
}