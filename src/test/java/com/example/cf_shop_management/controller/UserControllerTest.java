package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.UserCreateRequest;
import com.example.cf_shop_management.dto.request.UserUpdateRequest;
import com.example.cf_shop_management.dto.response.UserResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .fullName("Nguyễn Phi Dũng")
                .accountId(5L)
                .build();
    }

    @Test
    void createUser_Success() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("Nguyễn Phi Dũng")
                .accountId(5L)
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.fullName").value("Nguyễn Phi Dũng"))
                .andExpect(jsonPath("$.result.accountId").value(5L));
    }

    @Test
    void createUser_AccountAlreadyLinked_Returns4xx() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("Nguyễn Phi Dũng")
                .accountId(5L)
                .build();

        when(userService.createUser(any(UserCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.ACCOUNT_ALREADY_LINKED));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getUserById_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.fullName").value("Nguyễn Phi Dũng"));
    }

    @Test
    void getUserById_NotFound_Returns4xx() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXISTED));

        mockMvc.perform(get("/api/users/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllUsers_Success() throws Exception {
        UserResponse secondUser = UserResponse.builder()
                .id(2L)
                .fullName("Trần Tuấn Anh")
                .accountId(6L)
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(userResponse, secondUser));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[1].fullName").value("Trần Tuấn Anh"));
    }

    @Test
    void updateUser_Success() throws Exception {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("Phi Dũng")
                .build();

        UserResponse updated = UserResponse.builder()
                .id(1L)
                .fullName("Phi Dũng")
                .accountId(5L)
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.fullName").value("Phi Dũng"));
    }

    @Test
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa thông tin người dùng thành công"));

        verify(userService, times(1)).deleteUser(1L);
    }
}