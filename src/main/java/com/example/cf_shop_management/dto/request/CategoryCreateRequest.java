package com.example.cf_shop_management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryCreateRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    String name;

    @Size(max = 100, message = "Slug tối đa 100 ký tự")
    String slug;

    @Size(max = 255, message = "URL hình ảnh tối đa 255 ký tự")
    String imageUrl;

    Boolean isActive;

    Integer sortOrder;
}