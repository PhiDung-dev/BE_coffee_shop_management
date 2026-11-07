package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    Optional<PasswordReset> findByToken(String token);

    boolean existsByToken(String token);

    List<PasswordReset> findByAccountId(Long accountId);
}