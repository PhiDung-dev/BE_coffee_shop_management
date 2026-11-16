package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByInvoiceNumberIgnoreCase(String invoiceNumber);

    boolean existsByInvoiceNumberIgnoreCaseAndIdNot(String invoiceNumber, Long id);

    boolean existsByOrderId(Long orderId);

    Optional<Invoice> findByOrderId(Long orderId);

    Optional<Invoice> findByInvoiceNumberIgnoreCase(String invoiceNumber);

    @Query("""
            SELECT i FROM Invoice i
            LEFT JOIN i.order o
            LEFT JOIN i.taxProfile tp
            WHERE (:orderId IS NULL OR o.id = :orderId)
              AND (:taxProfileId IS NULL OR tp.id = :taxProfileId)
              AND (:fromDate IS NULL OR i.issuedAt >= :fromDate)
              AND (:toDate IS NULL OR i.issuedAt <= :toDate)
              AND (:keyword IS NULL
                   OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(tp.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(tp.taxCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Invoice> searchInvoices(
            @Param("orderId") Long orderId,
            @Param("taxProfileId") Long taxProfileId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}