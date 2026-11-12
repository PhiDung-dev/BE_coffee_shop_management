package com.example.cf_shop_management.service;

import com.example.cf_shop_management.dto.request.ProductCreateRequest;
import com.example.cf_shop_management.dto.request.ProductUpdateRequest;
import com.example.cf_shop_management.dto.response.ProductResponse;
import com.example.cf_shop_management.entity.Category;
import com.example.cf_shop_management.entity.Product;
import com.example.cf_shop_management.exception.AppException;
import com.example.cf_shop_management.exception.ErrorCode;
import com.example.cf_shop_management.mapper.ProductMapper;
import com.example.cf_shop_management.repository.CategoryRepository;
import com.example.cf_shop_management.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = productMapper.toProduct(request);
        product.setName(request.getName().trim());
        product.setCategory(category);

        if (product.getIsAvailable() == null) {
            product.setIsAvailable(true);
        }

        String rawSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? request.getSlug()
                : request.getName();
        product.setSlug(generateUniqueSlug(rawSlug));

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String keyword, Long categoryId, Boolean isAvailable, Pageable pageable) {
        String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return productRepository.searchProducts(normalizedKeyword, categoryId, isAvailable, pageable)
                .map(productMapper::toProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return productMapper.toProductResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            String trimmedName = request.getName().trim();
            if (productRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
                throw new AppException(ErrorCode.PRODUCT_EXISTED);
            }
            request.setName(trimmedName);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        productMapper.updateProduct(product, request);

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = toSlug(request.getSlug());
            if (!newSlug.equals(product.getSlug())) {
                product.setSlug(generateUniqueSlug(newSlug));
            }
        }

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getOrderItems() != null && !product.getOrderItems().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_IN_USE);
        }

        productRepository.delete(product);
    }

    private String generateUniqueSlug(String input) {
        String baseSlug = toSlug(input);
        String slug = baseSlug;
        int counter = 1;
        while (productRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(normalized).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return noAccent.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}