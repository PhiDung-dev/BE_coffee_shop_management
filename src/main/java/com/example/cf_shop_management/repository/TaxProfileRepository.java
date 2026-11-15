package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.TaxProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxProfileRepository extends JpaRepository<TaxProfile, Long> {

    boolean existsByAccountIdAndTaxCodeIgnoreCase(Long accountId, String taxCode);

    boolean existsByAccountIdAndTaxCodeIgnoreCaseAndIdNot(Long accountId, String taxCode, Long id);

    List<TaxProfile> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("""
            SELECT tp FROM TaxProfile tp
            LEFT JOIN tp.account a
            WHERE (:accountId IS NULL OR a.id = :accountId)
              AND (:keyword IS NULL
                   OR LOWER(tp.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(tp.taxCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(tp.companyAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<TaxProfile> searchTaxProfiles(
            @Param("accountId") Long accountId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}