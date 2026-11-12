package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAccountId(Long accountId);

    boolean existsByAccountId(Long accountId);
}