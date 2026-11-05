package com.example.cf_shop_management.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "title", length = 150)
    String title;

    @Column(name = "code", nullable = false, length = 50)
    String code;

    @Column(name = "discount_type", length = 20)
    String discountType;

    @Column(name = "discount_value", precision = 12, scale = 2)
    BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    BigDecimal maxDiscountAmount;

    @Column(name = "min_order_value", precision = 12, scale = 2)
    BigDecimal minOrderValue;

    @Column(name = "start_date")
    LocalDateTime startDate;

    @Column(name = "end_date")
    LocalDateTime endDate;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "usage_limit")
    Integer usageLimit;

    @Column(name = "used_count")
    Integer usedCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @Builder.Default
    Set<UserVoucher> userVouchers = new HashSet<>();

    @OneToMany(mappedBy = "appliedVoucher", fetch = FetchType.LAZY)
    @Builder.Default
    Set<Order> orders = new HashSet<>();
}