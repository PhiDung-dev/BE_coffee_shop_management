package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderCodeIgnoreCase(String orderCode);

    Optional<Order> findByOrderCodeIgnoreCase(String orderCode);

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN o.table t
            LEFT JOIN o.customerAccount ca
            LEFT JOIN o.staffAccount sa
            WHERE (:status IS NULL OR UPPER(o.status) = UPPER(:status))
              AND (:orderType IS NULL OR UPPER(o.orderType) = UPPER(:orderType))
              AND (:tableId IS NULL OR t.id = :tableId)
              AND (:customerAccountId IS NULL OR ca.id = :customerAccountId)
              AND (:staffAccountId IS NULL OR sa.id = :staffAccountId)
              AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
              AND (:toDate IS NULL OR o.createdAt <= :toDate)
              AND (:keyword IS NULL
                   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(t.tableNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(ca.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Order> searchOrders(
            @Param("status") String status,
            @Param("orderType") String orderType,
            @Param("tableId") Long tableId,
            @Param("customerAccountId") Long customerAccountId,
            @Param("staffAccountId") Long staffAccountId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}