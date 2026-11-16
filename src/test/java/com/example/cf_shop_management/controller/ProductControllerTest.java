package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.ProductCreateRequest;
import com.example.cf_shop_management.dto.request.ProductUpdateRequest;
import com.example.cf_shop_management.dto.response.ProductResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.ProductService;
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

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Cà phê sữa đá")
                .slug("ca-phe-sua-da")
                .basePrice(new BigDecimal("35000.00"))
                .imageUrl("https://cdn.cfshop.vn/products/cfsd.png")
                .isAvailable(true)
                .categoryId(2L)
                .categoryName("Cà phê truyền thống")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createProduct_Success() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Cà phê sữa đá")
                .basePrice(new BigDecimal("35000.00"))
                .categoryId(2L)
                .isAvailable(true)
                .build();

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("Cà phê sữa đá"))
                .andExpect(jsonPath("$.result.slug").value("ca-phe-sua-da"))
                .andExpect(jsonPath("$.result.basePrice").value(35000.00));
    }

    @Test
    void createProduct_NameExisted_Returns4xx() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Cà phê sữa đá")
                .basePrice(new BigDecimal("35000.00"))
                .categoryId(2L)
                .build();

        when(productService.createProduct(any(ProductCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.PRODUCT_EXISTED));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAllProducts_Paged_Success() throws Exception {
        Page<ProductResponse> pageResult = new PageImpl<>(
                List.of(productResponse),
                PageRequest.of(0, 10),
                1
        );

        when(productService.getAllProducts(eq("cà phê"), eq(2L), eq(true), any(Pageable.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/products")
                        .param("keyword", "cà phê")
                        .param("categoryId", "2")
                        .param("isAvailable", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].name").value("Cà phê sữa đá"))
                .andExpect(jsonPath("$.result.totalElements").value(1));
    }

    @Test
    void getProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.categoryName").value("Cà phê truyền thống"));
    }

    @Test
    void getProductById_NotFound_Returns4xx() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/api/products/{id}", 99L))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getProductBySlug_Success() throws Exception {
        when(productService.getProductBySlug("ca-phe-sua-da")).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/slug/{slug}", "ca-phe-sua-da"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.slug").value("ca-phe-sua-da"));
    }

    @Test
    void updateProduct_Success() throws Exception {
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name("Cà phê sữa Sài Gòn")
                .basePrice(new BigDecimal("39000.00"))
                .isAvailable(false)
                .build();

        ProductResponse updated = ProductResponse.builder()
                .id(1L)
                .name("Cà phê sữa Sài Gòn")
                .slug("ca-phe-sua-da")
                .basePrice(new BigDecimal("39000.00"))
                .isAvailable(false)
                .build();

        when(productService.updateProduct(eq(1L), any(ProductUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Cà phê sữa Sài Gòn"))
                .andExpect(jsonPath("$.result.basePrice").value(39000.00))
                .andExpect(jsonPath("$.result.isAvailable").value(false));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa sản phẩm thành công"));

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void deleteProduct_InUse_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.PRODUCT_IN_USE)).when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}