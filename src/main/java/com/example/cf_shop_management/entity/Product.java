package com.example.cf_shop_management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String slug;
    @Column(name = "base_price")
    private Double basePrice;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String image;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "sort_order")
    private Integer sortOrder;
    @Column(name = "category_id")
    private Long categoryId;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
