package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    Optional<Category> findBySlug(String slug);

    List<Category> findAllByOrderBySortOrderAsc();

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
}