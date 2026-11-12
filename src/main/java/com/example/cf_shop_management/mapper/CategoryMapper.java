package com.example.cf_shop_management.mapper;

import com.example.cf_shop_management.dto.request.CategoryCreateRequest;
import com.example.cf_shop_management.dto.request.CategoryUpdateRequest;
import com.example.cf_shop_management.dto.response.CategoryResponse;
import com.example.cf_shop_management.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toCategory(CategoryCreateRequest request);

    CategoryResponse toCategoryResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategory(CategoryUpdateRequest request, @MappingTarget Category category);
}