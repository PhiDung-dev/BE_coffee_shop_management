package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrue();
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findBySlug(String slug);
    List<Product> findByNameContainingIgnoreCase(String name);
}