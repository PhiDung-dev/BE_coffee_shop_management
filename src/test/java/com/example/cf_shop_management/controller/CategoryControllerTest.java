package com.example.cf_shop_management.controller;

import com.example.cf_shop_management.dto.request.CategoryCreateRequest;
import com.example.cf_shop_management.dto.request.CategoryUpdateRequest;
import com.example.cf_shop_management.dto.response.CategoryResponse;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.service.CategoryService;
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

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Cà phê truyền thống")
                .slug("ca-phe-truyen-thong")
                .imageUrl("https://example.com/coffee.png")
                .isActive(true)
                .sortOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCategory_Success() throws Exception {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Cà phê truyền thống")
                .slug("ca-phe-truyen-thong")
                .imageUrl("https://example.com/coffee.png")
                .isActive(true)
                .sortOrder(1)
                .build();

        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.name").value("Cà phê truyền thống"))
                .andExpect(jsonPath("$.result.slug").value("ca-phe-truyen-thong"))
                .andExpect(jsonPath("$.result.isActive").value(true));
    }

    @Test
    void createCategory_NameExisted_Returns4xx() throws Exception {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Cà phê truyền thống")
                .build();

        when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenThrow(new AppException(ErrorCode.CATEGORY_NAME_EXISTED));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getCategoryById_Success() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.slug").value("ca-phe-truyen-thong"));
    }

    @Test
    void getCategoryBySlug_Success() throws Exception {
        when(categoryService.getCategoryBySlug("ca-phe-truyen-thong")).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/categories/slug/{slug}", "ca-phe-truyen-thong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.slug").value("ca-phe-truyen-thong"));
    }

    @Test
    void getAllCategories_Success() throws Exception {
        CategoryResponse inactiveCategory = CategoryResponse.builder()
                .id(2L)
                .name("Đồ uống theo mùa")
                .slug("do-uong-theo-mua")
                .isActive(false)
                .sortOrder(2)
                .build();

        when(categoryService.getAllCategories()).thenReturn(List.of(categoryResponse, inactiveCategory));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getActiveCategories_Success() throws Exception {
        when(categoryService.getActiveCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].isActive").value(true));
    }

    @Test
    void updateCategory_Success() throws Exception {
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                .name("Cà phê Việt")
                .isActive(false)
                .build();

        CategoryResponse updated = CategoryResponse.builder()
                .id(1L)
                .name("Cà phê Việt")
                .slug("ca-phe-truyen-thong")
                .isActive(false)
                .build();

        when(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Cà phê Việt"))
                .andExpect(jsonPath("$.result.isActive").value(false));
    }

    @Test
    void deleteCategory_Success() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Xóa danh mục thành công"));
    }

    @Test
    void deleteCategory_HasProducts_Returns4xx() throws Exception {
        doThrow(new AppException(ErrorCode.CATEGORY_HAS_PRODUCTS)).when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().is4xxClientError());
    }
}