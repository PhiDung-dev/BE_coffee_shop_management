package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByOrderCode(String orderCode);

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByStatus(String status);

    List<Order> findByCustomerAccountIdOrderByCreatedAtDesc(Long customerAccountId);

    List<Order> findByTableIdAndStatusNotIn(Long tableId, List<String> excludedStatuses);
}
