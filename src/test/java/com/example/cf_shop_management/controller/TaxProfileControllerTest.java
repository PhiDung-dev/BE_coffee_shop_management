package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.TaxProfileCreateRequest;
import com.example.cf_shop_management.dto.request.TaxProfileUpdateRequest;
import com.example.cf_shop_management.dto.response.TaxProfileResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.TaxProfileService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaxProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaxProfileService taxProfileService;

    private TaxProfileResponse taxProfileResponse;

    @BeforeEach
    void setUp() {
        taxProfileResponse = TaxProfileResponse.builder()
                .id(1L)
                .accountId(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng, Việt Nam")
                .build();
    }

    @Test
    void createTaxProfile_Success() throws Exception {
        TaxProfileCreateRequest request = TaxProfileCreateRequest.builder()
                .accountId(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng, Việt Nam")
                .build();

        when(taxProfileService.createTaxProfile(any(TaxProfileCreateRequest.class))).thenReturn(taxProfileResponse);

        mockMvc.perform(post("/api/tax-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.accountId").value(5L))
                .andExpect(jsonPath("$.result.companyName").value("Công ty TNHH Công Nghệ ABC"))
                .andExpect(jsonPath("$.result.taxCode").value("0401234567"));
    }

    @Test
    void createTaxProfile_TaxCodeExisted_Returns4xx() throws Exception {
        TaxProfileCreateRequest request = TaxProfileCreateRequest.builder()
                .accountId(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng, Việt Nam")
                .build();

        when(taxProfileService.createTaxProfile(any(TaxProfileCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.TAX_PROFILE_EXISTED));

        mockMvc.perform(post("/api/tax-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllTaxProfiles_Paged_Success() throws Exception {
        Page<TaxProfileResponse> pageResult = new PageImpl<>(
                List.of(taxProfileResponse),
                PageRequest.of(0, 10),
                1
        );

        when(taxProfileService.getAllTaxProfiles(eq(5L), eq("040123"), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/tax-profiles")
                        .param("accountId", "5")
                        .param("keyword", "040123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].taxCode").value("0401234567"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getTaxProfileById_Success() throws Exception {
        when(taxProfileService.getTaxProfileById(1L)).thenReturn(taxProfileResponse);

        mockMvc.perform(get("/api/tax-profiles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.companyAddress").value("Đà Nẵng, Việt Nam"));
    }

    @Test
    void getTaxProfileById_NotFound_Returns4xx() throws Exception {
        when(taxProfileService.getTaxProfileById(99L))
                .thenThrow(new AppException(ErrorCode.TAX_PROFILE_NOT_FOUND));

        mockMvc.perform(get("/api/tax-profiles/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getTaxProfilesByAccountId_Success() throws Exception {
        when(taxProfileService.getTaxProfilesByAccountId(5L)).thenReturn(List.of(taxProfileResponse));

        mockMvc.perform(get("/api/tax-profiles/account/{accountId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].accountId").value(5L));
    }

    @Test
    void updateTaxProfile_Success() throws Exception {
        TaxProfileUpdateRequest request = TaxProfileUpdateRequest.builder()
                .companyName("Công ty Cổ phần ABC")
                .taxCode("0401234567")
                .companyAddress("Hải Châu, Đà Nẵng")
                .build();

        TaxProfileResponse updated = TaxProfileResponse.builder()
                .id(1L)
                .accountId(5L)
                .companyName("Công ty Cổ phần ABC")
                .taxCode("0401234567")
                .companyAddress("Hải Châu, Đà Nẵng")
                .build();

        when(taxProfileService.updateTaxProfile(eq(1L), any(TaxProfileUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tax-profiles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.companyName").value("Công ty Cổ phần ABC"))
                .andExpect(jsonPath("$.result.companyAddress").value("Hải Châu, Đà Nẵng"));
    }

    @Test
    void deleteTaxProfile_Success() throws Exception {
        doNothing().when(taxProfileService).deleteTaxProfile(1L);

        mockMvc.perform(delete("/api/tax-profiles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa thông tin xuất hóa đơn thành công"));

        verify(taxProfileService, times(1)).deleteTaxProfile(1L);
    }

    @Test
    void deleteTaxProfile_InUse_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.TAX_PROFILE_IN_USE)).when(taxProfileService).deleteTaxProfile(1L);

        mockMvc.perform(delete("/api/tax-profiles/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}