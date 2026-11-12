package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsBySlug(String slug);

    List<Product> findByIsAvailableTrue();
    List<Product> findByCategoryId(Long categoryId);
    Optional<Product> findBySlug(String slug);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN p.category c
            WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:isAvailable IS NULL OR p.isAvailable = :isAvailable)
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("isAvailable") Boolean isAvailable,
            Pageable pageable
    );
    List<Product> findByNameContainingIgnoreCase(String name);
}