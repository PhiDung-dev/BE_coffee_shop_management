package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.ProductCreateRequest;
import com.example.cf_shop_management.dto.request.ProductUpdateRequest;
import com.example.cf_shop_management.dto.response.ProductResponse;
import com.example.cf_shop_management.entity.Category;
import com.example.cf_shop_management.entity.OrderItem;
import com.example.cf_shop_management.entity.Product;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.ProductMapper;
import com.example.cf_shop_management.repository.CategoryRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(2L)
                .name("Cà phê truyền thống")
                .build();

        product = Product.builder()
                .id(1L)
                .name("Cà phê sữa đá")
                .slug("ca-phe-sua-da")
                .basePrice(new BigDecimal("35000.00"))
                .isAvailable(true)
                .category(category)
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .name("Cà phê sữa đá")
                .slug("ca-phe-sua-da")
                .basePrice(new BigDecimal("35000.00"))
                .isAvailable(true)
                .categoryId(2L)
                .categoryName("Cà phê truyền thống")
                .build();
    }

    @Test
    void createProduct_AutoGeneratesUniqueSlugAndDefaults_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("  Cà phê sữa đá  ")
                .basePrice(new BigDecimal("35000.00"))
                .categoryId(2L)
                .build();

        Product mappedProduct = new Product();

        when(productRepository.existsByNameIgnoreCase("Cà phê sữa đá")).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(productMapper.toProduct(request)).thenReturn(mappedProduct);
        when(productRepository.existsBySlug("ca-phe-sua-da")).thenReturn(true);
        when(productRepository.existsBySlug("ca-phe-sua-da-1")).thenReturn(false);
        when(productRepository.save(mappedProduct)).thenReturn(product);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Cà phê sữa đá", mappedProduct.getName());
        assertEquals("ca-phe-sua-da-1", mappedProduct.getSlug());
        assertEquals(true, mappedProduct.getIsAvailable());
        assertEquals(category, mappedProduct.getCategory());
        verify(productRepository).save(mappedProduct);
    }

    @Test
    void createProduct_NameExisted_ThrowsException() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("  Cà phê sữa đá  ")
                .basePrice(new BigDecimal("35000.00"))
                .categoryId(2L)
                .build();

        when(productRepository.existsByNameIgnoreCase("Cà phê sữa đá")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> productService.createProduct(request));

        assertEquals(ErrorCode.PRODUCT_EXISTED, exception.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsException() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("Bạc xỉu")
                .basePrice(new BigDecimal("35000.00"))
                .categoryId(99L)
                .build();

        when(productRepository.existsByNameIgnoreCase("Bạc xỉu")).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> productService.createProduct(request));

        assertEquals(ErrorCode.CATEGORY_NOT_FOUND, exception.getErrorCode());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_Paged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.searchProducts("cà phê", 2L, true, pageable)).thenReturn(page);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        Page<ProductResponse> result = productService.getAllProducts("  cà phê  ", 2L, true, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Cà phê sữa đá", result.getContent().get(0).getName());
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> productService.getProductById(99L));

        assertEquals(ErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getProductBySlug_Success() {
        when(productRepository.findBySlug("ca-phe-sua-da")).thenReturn(Optional.of(product));
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.getProductBySlug("  ca-phe-sua-da  ");

        assertEquals("ca-phe-sua-da", result.getSlug());
    }

    @Test
    void updateProduct_Success() {
        Category newCategory = Category.builder().id(5L).name("Đặc sản").build();
        ProductUpdateRequest request = ProductUpdateRequest.builder()
                .name("  Cà phê sữa Sài Gòn  ")
                .slug("ca-phe-sua-sai-gon")
                .categoryId(5L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsByNameIgnoreCaseAndIdNot("Cà phê sữa Sài Gòn", 1L)).thenReturn(false);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(newCategory));
        when(productRepository.existsBySlug("ca-phe-sua-sai-gon")).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);

        ProductResponse result = productService.updateProduct(1L, request);

        assertNotNull(result);
        assertEquals("Cà phê sữa Sài Gòn", request.getName());
        assertEquals("ca-phe-sua-sai-gon", product.getSlug());
        assertEquals(newCategory, product.getCategory());
        verify(productMapper).updateProduct(request, product);
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_InUse_ThrowsException() {
        product.setOrderItems(Set.of(OrderItem.builder().id(1L).build()));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        AppException exception = assertThrows(AppException.class,
                () -> productService.deleteProduct(1L));

        assertEquals(ErrorCode.PRODUCT_IN_USE, exception.getErrorCode());
        verify(productRepository, never()).delete(any());
    }
}