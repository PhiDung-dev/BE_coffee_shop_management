package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.CategoryCreateRequest;
import com.example.cf_shop_management.dto.request.CategoryUpdateRequest;
import com.example.cf_shop_management.dto.response.CategoryResponse;
import com.example.cf_shop_management.entity.Category;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.CategoryMapper;
import com.example.cf_shop_management.repository.CategoryRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Đồ uống đặc biệt")
                .slug("do-uong-dac-biet")
                .isActive(true)
                .sortOrder(0)
                .build();

        categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Đồ uống đặc biệt")
                .slug("do-uong-dac-biet")
                .isActive(true)
                .sortOrder(0)
                .build();
    }

    @Test
    void createCategory_AutoGeneratesSlugAndDefaults_Success() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("  Đồ uống đặc biệt  ")
                .build();

        Category mappedCategory = new Category();

        when(categoryRepository.existsByName("Đồ uống đặc biệt")).thenReturn(false);
        when(categoryRepository.existsBySlug("do-uong-dac-biet")).thenReturn(false);
        when(categoryMapper.toCategory(request)).thenReturn(mappedCategory);
        when(categoryRepository.save(mappedCategory)).thenReturn(category);
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("Đồ uống đặc biệt", mappedCategory.getName());
        assertEquals("do-uong-dac-biet", mappedCategory.getSlug());
        assertEquals(true, mappedCategory.getIsActive());
        assertEquals(0, mappedCategory.getSortOrder());
        verify(categoryRepository).save(mappedCategory);
    }

    @Test
    void createCategory_CustomSlugProvided_Success() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Cà phê")
                .slug("  ca-phe-viet  ")
                .isActive(false)
                .sortOrder(3)
                .build();

        Category mappedCategory = Category.builder()
                .isActive(false)
                .sortOrder(3)
                .build();

        when(categoryRepository.existsByName("Cà phê")).thenReturn(false);
        when(categoryRepository.existsBySlug("ca-phe-viet")).thenReturn(false);
        when(categoryMapper.toCategory(request)).thenReturn(mappedCategory);
        when(categoryRepository.save(mappedCategory)).thenReturn(category);
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        categoryService.createCategory(request);

        assertEquals("ca-phe-viet", mappedCategory.getSlug());
        assertEquals(false, mappedCategory.getIsActive());
        assertEquals(3, mappedCategory.getSortOrder());
    }

    @Test
    void createCategory_NameExisted_ThrowsException() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Đồ uống đặc biệt")
                .build();

        when(categoryRepository.existsByName("Đồ uống đặc biệt")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.createCategory(request));

        assertEquals(ErrorCode.CATEGORY_NAME_EXISTED, exception.getErrorCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_SlugExisted_ThrowsException() {
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .name("Đồ uống đặc biệt")
                .slug("do-uong-dac-biet")
                .build();

        when(categoryRepository.existsByName("Đồ uống đặc biệt")).thenReturn(false);
        when(categoryRepository.existsBySlug("do-uong-dac-biet")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.createCategory(request));

        assertEquals(ErrorCode.CATEGORY_SLUG_EXISTED, exception.getErrorCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_Success() {
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                .name("  Trà trái cây  ")
                .slug("  tra-trai-cay  ")
                .isActive(false)
                .build();

        CategoryResponse updatedResponse = CategoryResponse.builder()
                .id(1L)
                .name("Trà trái cây")
                .slug("tra-trai-cay")
                .isActive(false)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Trà trái cây", 1L)).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCaseAndIdNot("tra-trai-cay", 1L)).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toCategoryResponse(category)).thenReturn(updatedResponse);

        CategoryResponse result = categoryService.updateCategory(1L, request);

        assertEquals("Trà trái cây", request.getName());
        assertEquals("tra-trai-cay", request.getSlug());
        assertEquals("Trà trái cây", result.getName());
        verify(categoryMapper).updateCategory(request, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        CategoryUpdateRequest request = CategoryUpdateRequest.builder().name("Trà").build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.updateCategory(99L, request));

        assertEquals(ErrorCode.CATEGORY_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void updateCategory_NameExisted_ThrowsException() {
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                .name("Trà trái cây")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Trà trái cây", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.updateCategory(1L, request));

        assertEquals(ErrorCode.CATEGORY_NAME_EXISTED, exception.getErrorCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_SlugExisted_ThrowsException() {
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
                .slug("tra-trai-cay")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugIgnoreCaseAndIdNot("tra-trai-cay", 1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.updateCategory(1L, request));

        assertEquals(ErrorCode.CATEGORY_SLUG_EXISTED, exception.getErrorCode());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.getCategoryById(99L));

        assertEquals(ErrorCode.CATEGORY_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getCategoryBySlug_Success() {
        when(categoryRepository.findBySlug("do-uong-dac-biet")).thenReturn(Optional.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.getCategoryBySlug("do-uong-dac-biet");

        assertEquals("do-uong-dac-biet", result.getSlug());
    }

    @Test
    void getCategoryBySlug_NotFound_ThrowsException() {
        when(categoryRepository.findBySlug("khong-ton-tai")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.getCategoryBySlug("khong-ton-tai"));

        assertEquals(ErrorCode.CATEGORY_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
    }

    @Test
    void getActiveCategories_Success() {
        when(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(category));
        when(categoryMapper.toCategoryResponse(category)).thenReturn(categoryResponse);

        List<CategoryResponse> result = categoryService.getActiveCategories();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void deleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(false);
        doNothing().when(categoryRepository).delete(category);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.deleteCategory(99L));

        assertEquals(ErrorCode.CATEGORY_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void deleteCategory_HasProducts_ThrowsException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryId(1L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> categoryService.deleteCategory(1L));

        assertEquals(ErrorCode.CATEGORY_HAS_PRODUCTS, exception.getErrorCode());
        verify(categoryRepository, never()).delete(any());
    }
}