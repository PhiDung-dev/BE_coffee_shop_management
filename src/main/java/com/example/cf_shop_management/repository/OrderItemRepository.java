package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    @Query("""
            SELECT oi FROM OrderItem oi
            LEFT JOIN oi.order o
            LEFT JOIN oi.product p
            WHERE (:orderId IS NULL OR o.id = :orderId)
              AND (:productId IS NULL OR p.id = :productId)
            """)
    Page<OrderItem> searchOrderItems(
            @Param("orderId") Long orderId,
            @Param("productId") Long productId,
            Pageable pageable
    );
}