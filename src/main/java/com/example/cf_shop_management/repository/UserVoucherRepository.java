package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.UserVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    boolean existsByAccountIdAndVoucherId(Long accountId, Long voucherId);

    List<UserVoucher> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Query("""
            SELECT uv FROM UserVoucher uv
            LEFT JOIN uv.account a
            LEFT JOIN uv.voucher v
            WHERE (:accountId IS NULL OR a.id = :accountId)
              AND (:voucherId IS NULL OR v.id = :voucherId)
              AND (:isUsed IS NULL OR uv.isUsed = :isUsed)
            """)
    Page<UserVoucher> searchUserVouchers(
            @Param("accountId") Long accountId,
            @Param("voucherId") Long voucherId,
            @Param("isUsed") Boolean isUsed,
            Pageable pageable
    );
}