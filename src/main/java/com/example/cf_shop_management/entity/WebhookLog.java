package com.example.cf_shop_management.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "gateway", nullable = false, length = 50)
    String gateway;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    String payload;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    String status = "RECEIVED";

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}