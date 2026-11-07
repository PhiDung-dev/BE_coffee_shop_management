package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    boolean existsByCode(String code);

    Optional<Voucher> findByCode(String code);

    List<Voucher> findByIsActiveTrue();
}