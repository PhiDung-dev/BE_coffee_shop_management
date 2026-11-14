package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Long> {

    boolean existsByTableNumber(String tableNumber);

    Optional<Tables> findByTableNumber(String tableNumber);

    List<Tables> findByAreaId(Long areaId);

    List<Tables> findByStatus(String status);

    List<Tables> findByAreaIdAndStatus(Long areaId, String status);
}
