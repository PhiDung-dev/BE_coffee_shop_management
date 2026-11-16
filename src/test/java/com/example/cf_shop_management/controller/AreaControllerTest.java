package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.AreaCreateRequest;
import com.example.cf_shop_management.dto.request.AreaUpdateRequest;
import com.example.cf_shop_management.dto.response.AreaResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.AreaService;
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

@WebMvcTest(AreaController.class)
@AutoConfigureMockMvc(addFilters = false)
class AreaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AreaService areaService;

    private AreaResponse areaResponse;

    @BeforeEach
    void setUp() {
        areaResponse = AreaResponse.builder()
                .id(1L)
                .name("Tầng 1 - Trong nhà")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createArea_Success() throws Exception {
        AreaCreateRequest request = AreaCreateRequest.builder()
                .name("Tầng 1 - Trong nhà")
                .build();

        when(areaService.createArea(any(AreaCreateRequest.class))).thenReturn(areaResponse);

        mockMvc.perform(post("/api/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("Tầng 1 - Trong nhà"));
    }

    @Test
    void createArea_Existed_Returns4xx() throws Exception {
        AreaCreateRequest request = AreaCreateRequest.builder()
                .name("Tầng 1 - Trong nhà")
                .build();

        when(areaService.createArea(any(AreaCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.AREA_EXISTED));

        mockMvc.perform(post("/api/areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllAreas_Paged_Success() throws Exception {
        Page<AreaResponse> pageResult = new PageImpl<>(
                List.of(areaResponse),
                PageRequest.of(0, 10),
                1
        );

        when(areaService.getAllAreas(eq("Tầng 1"), any(Pageable.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/areas")
                        .param("keyword", "Tầng 1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].name").value("Tầng 1 - Trong nhà"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getAllAreasList_Success() throws Exception {
        AreaResponse secondArea = AreaResponse.builder()
                .id(2L)
                .name("Sân thượng")
                .build();

        when(areaService.getAllAreasList()).thenReturn(List.of(areaResponse, secondArea));

        mockMvc.perform(get("/api/areas/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[1].name").value("Sân thượng"));
    }

    @Test
    void getAreaById_Success() throws Exception {
        when(areaService.getAreaById(1L)).thenReturn(areaResponse);

        mockMvc.perform(get("/api/areas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("Tầng 1 - Trong nhà"));
    }

    @Test
    void updateArea_Success() throws Exception {
        AreaUpdateRequest request = AreaUpdateRequest.builder()
                .name("Tầng 1 - VIP")
                .build();

        AreaResponse updated = AreaResponse.builder()
                .id(1L)
                .name("Tầng 1 - VIP")
                .build();

        when(areaService.updateArea(eq(1L), any(AreaUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/areas/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Tầng 1 - VIP"));
    }

    @Test
    void deleteArea_Success() throws Exception {
        doNothing().when(areaService).deleteArea(1L);

        mockMvc.perform(delete("/api/areas/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa khu vực thành công"));
    }

    @Test
    void deleteArea_InUse_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.AREA_IN_USE)).when(areaService).deleteArea(1L);

        mockMvc.perform(delete("/api/areas/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}