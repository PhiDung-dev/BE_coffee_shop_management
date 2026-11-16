package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.VoucherCreateRequest;
import com.example.cf_shop_management.dto.request.VoucherUpdateRequest;
import com.example.cf_shop_management.dto.response.VoucherResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.VoucherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
@AutoConfigureMockMvc(addFilters = false)
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VoucherService voucherService;

    private VoucherResponse voucherResponse;

    @BeforeEach
    void setUp() {
        voucherResponse = VoucherResponse.builder()
                .id(1L)
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .minOrderValue(new BigDecimal("50000.00"))
                .maxDiscountAmount(new BigDecimal("20000.00"))
                .usageLimit(100)
                .usedCount(0)
                .startDate(LocalDateTime.of(2026, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 12, 31, 23, 59))
                .isActive(true)
                .build();
    }

    @Test
    void createVoucher_Success() throws Exception {
        VoucherCreateRequest request = VoucherCreateRequest.builder()
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .minOrderValue(new BigDecimal("50000.00"))
                .maxDiscountAmount(new BigDecimal("20000.00"))
                .usageLimit(100)
                .startDate(LocalDateTime.of(2026, 11, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 12, 31, 23, 59))
                .isActive(true)
                .build();

        when(voucherService.createVoucher(any(VoucherCreateRequest.class))).thenReturn(voucherResponse);

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.code").value("WELCOME10"))
                .andExpect(jsonPath("$.result.discountType").value("PERCENTAGE"))
                .andExpect(jsonPath("$.result.discountValue").value(10.00))
                .andExpect(jsonPath("$.result.usedCount").value(0))
                .andExpect(jsonPath("$.result.isActive").value(true));
    }

    @Test
    void createVoucher_CodeExisted_Returns4xx() throws Exception {
        VoucherCreateRequest request = VoucherCreateRequest.builder()
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("10.00"))
                .build();

        when(voucherService.createVoucher(any(VoucherCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.VOUCHER_CODE_EXISTED));

        mockMvc.perform(post("/api/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getVoucherById_Success() throws Exception {
        when(voucherService.getVoucherById(1L)).thenReturn(voucherResponse);

        mockMvc.perform(get("/api/vouchers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.code").value("WELCOME10"));
    }

    @Test
    void getVoucherById_NotFound_Returns4xx() throws Exception {
        when(voucherService.getVoucherById(99L))
                .thenThrow(new AppException(ErrorCode.VOUCHER_NOT_EXISTED));

        mockMvc.perform(get("/api/vouchers/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getVoucherByCode_Success() throws Exception {
        when(voucherService.getVoucherByCode("WELCOME10")).thenReturn(voucherResponse);

        mockMvc.perform(get("/api/vouchers/code/{code}", "WELCOME10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.code").value("WELCOME10"));
    }

    @Test
    void getAllVouchers_Success() throws Exception {
        VoucherResponse inactiveVoucher = VoucherResponse.builder()
                .id(2L)
                .code("SUMMER20")
                .discountType("FIXED_AMOUNT")
                .discountValue(new BigDecimal("20000.00"))
                .isActive(false)
                .build();

        when(voucherService.getAllVouchers()).thenReturn(List.of(voucherResponse, inactiveVoucher));

        mockMvc.perform(get("/api/vouchers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getActiveVouchers_Success() throws Exception {
        when(voucherService.getActiveVouchers()).thenReturn(List.of(voucherResponse));

        mockMvc.perform(get("/api/vouchers/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].isActive").value(true));
    }

    @Test
    void updateVoucher_Success() throws Exception {
        VoucherUpdateRequest request = VoucherUpdateRequest.builder()
                .discountValue(new BigDecimal("15.00"))
                .isActive(false)
                .build();

        VoucherResponse updated = VoucherResponse.builder()
                .id(1L)
                .code("WELCOME10")
                .discountType("PERCENTAGE")
                .discountValue(new BigDecimal("15.00"))
                .isActive(false)
                .build();

        when(voucherService.updateVoucher(eq(1L), any(VoucherUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/vouchers/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.discountValue").value(15.00))
                .andExpect(jsonPath("$.result.isActive").value(false));
    }

    @Test
    void deleteVoucher_Success() throws Exception {
        doNothing().when(voucherService).deleteVoucher(1L);

        mockMvc.perform(delete("/api/vouchers/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa voucher thành công"));

        verify(voucherService, times(1)).deleteVoucher(1L);
    }
}