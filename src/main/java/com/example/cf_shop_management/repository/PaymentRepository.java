package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByTransactionCode(String transactionCode);

    boolean existsByTransactionCodeAndIdNot(String transactionCode, Long id);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.order.id = :orderId
              AND UPPER(p.status) = 'SUCCESS'
            """)
    BigDecimal sumPaidAmountByOrderId(@Param("orderId") Long orderId);

    @Query("""
            SELECT p FROM Payment p
            LEFT JOIN p.order o
            WHERE (:orderId IS NULL OR o.id = :orderId)
              AND (:status IS NULL OR UPPER(p.status) = UPPER(:status))
              AND (:paymentMethod IS NULL OR UPPER(p.paymentMethod) = UPPER(:paymentMethod))
              AND (:paymentGateway IS NULL OR UPPER(p.paymentGateway) = UPPER(:paymentGateway))
              AND (:keyword IS NULL
                   OR LOWER(p.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Payment> searchPayments(
            @Param("orderId") Long orderId,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("paymentGateway") String paymentGateway,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}