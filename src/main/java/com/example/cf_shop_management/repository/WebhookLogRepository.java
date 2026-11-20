package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {

    List<WebhookLog> findByStatusOrderByCreatedAtDesc(String status);

    List<WebhookLog> findByGatewayOrderByCreatedAtDesc(String gateway);

    @Query("""
            SELECT w FROM WebhookLog w
            WHERE (:gateway IS NULL OR LOWER(w.gateway) = LOWER(:gateway))
              AND (:status IS NULL OR LOWER(w.status) = LOWER(:status))
              AND (:fromDate IS NULL OR w.createdAt >= :fromDate)
              AND (:toDate IS NULL OR w.createdAt <= :toDate)
              AND (:keyword IS NULL OR LOWER(w.payload) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<WebhookLog> searchWebhookLogs(
            @Param("gateway") String gateway,
            @Param("status") String status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}