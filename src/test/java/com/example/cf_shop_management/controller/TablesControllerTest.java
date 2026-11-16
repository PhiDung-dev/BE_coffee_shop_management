package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.TablesCreateRequest;
import com.example.cf_shop_management.dto.request.TablesUpdateRequest;
import com.example.cf_shop_management.dto.response.TablesResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.TablesService;
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

@WebMvcTest(TablesController.class)
@AutoConfigureMockMvc(addFilters = false)
class TablesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TablesService tablesService;

    private TablesResponse tablesResponse;

    @BeforeEach
    void setUp() {
        tablesResponse = TablesResponse.builder()
                .id(1L)
                .tableNumber("B01")
                .capacity(4)
                .status("AVAILABLE")
                .areaId(1L)
                .areaName("Tầng 1 - Trong nhà")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createTable_Success() throws Exception {
        TablesCreateRequest request = TablesCreateRequest.builder()
                .tableNumber("B01")
                .capacity(4)
                .status("AVAILABLE")
                .areaId(1L)
                .build();

        when(tablesService.createTable(any(TablesCreateRequest.class))).thenReturn(tablesResponse);

        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.tableNumber").value("B01"))
                .andExpect(jsonPath("$.result.capacity").value(4))
                .andExpect(jsonPath("$.result.status").value("AVAILABLE"));
    }

    @Test
    void createTable_NumberExisted_Returns4xx() throws Exception {
        TablesCreateRequest request = TablesCreateRequest.builder()
                .tableNumber("B01")
                .capacity(4)
                .areaId(1L)
                .build();

        when(tablesService.createTable(any(TablesCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.TABLE_NUMBER_EXISTED));

        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTableById_Success() throws Exception {
        when(tablesService.getTableById(1L)).thenReturn(tablesResponse);

        mockMvc.perform(get("/api/tables/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.tableNumber").value("B01"));
    }

    @Test
    void getTableById_NotFound_Returns4xx() throws Exception {
        when(tablesService.getTableById(99L))
                .thenThrow(new AppException(ErrorCode.TABLE_NOT_EXISTED));

        mockMvc.perform(get("/api/tables/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTableByNumber_Success() throws Exception {
        when(tablesService.getTableByNumber("B01")).thenReturn(tablesResponse);

        mockMvc.perform(get("/api/tables/number/{tableNumber}", "B01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tableNumber").value("B01"));
    }

    @Test
    void getAllTables_Success() throws Exception {
        when(tablesService.getAllTables()).thenReturn(List.of(tablesResponse));

        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    @Test
    void getTablesByArea_Success() throws Exception {
        when(tablesService.getTablesByArea(1L)).thenReturn(List.of(tablesResponse));

        mockMvc.perform(get("/api/tables/area/{areaId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].areaId").value(1L));
    }

    @Test
    void getTablesByStatus_Success() throws Exception {
        when(tablesService.getTablesByStatus("AVAILABLE")).thenReturn(List.of(tablesResponse));

        mockMvc.perform(get("/api/tables/status/{status}", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].status").value("AVAILABLE"));
    }

    @Test
    void updateTable_Success() throws Exception {
        TablesUpdateRequest request = TablesUpdateRequest.builder()
                .tableNumber("B01-VIP")
                .capacity(6)
                .build();

        TablesResponse updated = TablesResponse.builder()
                .id(1L)
                .tableNumber("B01-VIP")
                .capacity(6)
                .status("AVAILABLE")
                .build();

        when(tablesService.updateTable(eq(1L), any(TablesUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tables/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tableNumber").value("B01-VIP"))
                .andExpect(jsonPath("$.result.capacity").value(6));
    }

    @Test
    void updateTableStatus_Success() throws Exception {
        TablesResponse occupied = TablesResponse.builder()
                .id(1L)
                .tableNumber("B01")
                .status("OCCUPIED")
                .build();

        when(tablesService.updateTableStatus(1L, "OCCUPIED")).thenReturn(occupied);

        mockMvc.perform(patch("/api/tables/{id}/status", 1L)
                        .param("status", "OCCUPIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("OCCUPIED"));
    }

    @Test
    void deleteTable_Success() throws Exception {
        doNothing().when(tablesService).deleteTable(1L);

        mockMvc.perform(delete("/api/tables/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa bàn thành công"));

        verify(tablesService, times(1)).deleteTable(1L);
    }
}