package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.InvoiceCreateRequest;
import com.example.cf_shop_management.dto.request.InvoiceUpdateRequest;
import com.example.cf_shop_management.dto.response.InvoiceResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.InvoiceService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;

    private InvoiceResponse invoiceResponse;

    @BeforeEach
    void setUp() {
        invoiceResponse = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-20261116-A1B2C3")
                .issuedAt(LocalDateTime.of(2026, 11, 16, 14, 30))
                .pdfUrl("https://cdn.cfshop.vn/invoices/INV-20261116-A1B2C3.pdf")
                .orderId(10L)
                .orderCode("ORD-20261116-XYZ123")
                .orderFinalAmount(new BigDecimal("120000.00"))
                .taxProfileId(5L)
                .companyName("Công ty TNHH Công Nghệ ABC")
                .taxCode("0401234567")
                .companyAddress("Đà Nẵng, Việt Nam")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createInvoice_Success() throws Exception {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(10L)
                .taxProfileId(5L)
                .build();

        when(invoiceService.createInvoice(any(InvoiceCreateRequest.class))).thenReturn(invoiceResponse);

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.invoiceNumber").value("INV-20261116-A1B2C3"))
                .andExpect(jsonPath("$.result.orderId").value(10L))
                .andExpect(jsonPath("$.result.taxCode").value("0401234567"));
    }

    @Test
    void createInvoice_AlreadyExisted_Returns4xx() throws Exception {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(10L)
                .taxProfileId(5L)
                .build();

        when(invoiceService.createInvoice(any(InvoiceCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.INVOICE_ALREADY_EXISTED_FOR_ORDER));

        mockMvc.perform(post("/api/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllInvoices_Paged_Success() throws Exception {
        Page<InvoiceResponse> pageResult = new PageImpl<>(
                List.of(invoiceResponse),
                PageRequest.of(0, 10),
                1
        );

        when(invoiceService.getAllInvoices(
                eq(10L),
                eq(5L),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq("INV-2026"),
                any(Pageable.class)
        )).thenReturn(pageResult);

        mockMvc.perform(get("/api/invoices")
                        .param("orderId", "10")
                        .param("taxProfileId", "5")
                        .param("fromDate", "2026-11-01T00:00:00")
                        .param("toDate", "2026-11-30T23:59:59")
                        .param("keyword", "INV-2026")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].invoiceNumber").value("INV-20261116-A1B2C3"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getInvoiceById_Success() throws Exception {
        when(invoiceService.getInvoiceById(1L)).thenReturn(invoiceResponse);

        mockMvc.perform(get("/api/invoices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.companyName").value("Công ty TNHH Công Nghệ ABC"));
    }

    @Test
    void getInvoiceById_NotFound_Returns4xx() throws Exception {
        when(invoiceService.getInvoiceById(99L))
                .thenThrow(new AppException(ErrorCode.INVOICE_NOT_FOUND));

        mockMvc.perform(get("/api/invoices/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getInvoiceByOrderId_Success() throws Exception {
        when(invoiceService.getInvoiceByOrderId(10L)).thenReturn(invoiceResponse);

        mockMvc.perform(get("/api/invoices/order/{orderId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderId").value(10L))
                .andExpect(jsonPath("$.result.invoiceNumber").value("INV-20261116-A1B2C3"));
    }

    @Test
    void getInvoiceByNumber_Success() throws Exception {
        when(invoiceService.getInvoiceByNumber("INV-20261116-A1B2C3")).thenReturn(invoiceResponse);

        mockMvc.perform(get("/api/invoices/number/{invoiceNumber}", "INV-20261116-A1B2C3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.invoiceNumber").value("INV-20261116-A1B2C3"));
    }

    @Test
    void updateInvoice_Success() throws Exception {
        InvoiceUpdateRequest request = InvoiceUpdateRequest.builder()
                .taxProfileId(8L)
                .build();

        InvoiceResponse updated = InvoiceResponse.builder()
                .id(1L)
                .invoiceNumber("INV-20261116-A1B2C3")
                .taxProfileId(8L)
                .companyName("Công ty Cổ phần Mới")
                .taxCode("0409876543")
                .build();

        when(invoiceService.updateInvoice(eq(1L), any(InvoiceUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/invoices/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.taxProfileId").value(8L))
                .andExpect(jsonPath("$.result.companyName").value("Công ty Cổ phần Mới"));
    }

    @Test
    void deleteInvoice_Success() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/api/invoices/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa hóa đơn thành công"));

        verify(invoiceService, times(1)).deleteInvoice(1L);
    }
}