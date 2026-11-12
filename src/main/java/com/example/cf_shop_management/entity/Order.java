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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "order_code", length = 50)
    String orderCode;

    @Column(name = "order_type", length = 20)
    String orderType;

    @Column(name = "status", length = 20)
    String status;

    @Column(name = "subtotal", precision = 12, scale = 2)
    BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    BigDecimal discountAmount;

    @Column(name = "final_amount", precision = 12, scale = 2)
    BigDecimal finalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    Tables table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_account_id")
    Account customerAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_account_id")
    Account staffAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_voucher_id")
    Voucher appliedVoucher;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<OrderItem> orderItems = new HashSet<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @Builder.Default
    Set<Payment> payments = new HashSet<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    Invoice invoice;
}