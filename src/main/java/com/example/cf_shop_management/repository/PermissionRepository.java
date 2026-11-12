package com.example.cf_shop_management.repository;

import com.example.cf_shop_management.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByCode(String code);
    Optional<Permission> findByCode(String code);
    List<Permission> findAllByCodeIn(Set<String> codes);
    List<Permission> findAllByIdIn(Set<Long> ids);
}