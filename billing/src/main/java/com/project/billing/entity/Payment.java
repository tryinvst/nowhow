package com.project.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Журнал оплат: сейчас provider=stub, при подключении ЮKassa сюда ляжет её payment id.
 */
@Entity
@Table(name = "payments", indexes = @Index(name = "idx_payments_user", columnList = "user_id"))
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String planCode;
    private long amountKopecks;
    @Column(nullable = false)
    private String provider;
    @Column(nullable = false)
    private String status;
    private Instant createdAt;

    public Payment() {
    }

    public Payment(Long userId, String planCode, long amountKopecks, String provider, String status) {
        this.userId = userId;
        this.planCode = planCode;
        this.amountKopecks = amountKopecks;
        this.provider = provider;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public long getAmountKopecks() {
        return amountKopecks;
    }

    public String getProvider() {
        return provider;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void beforeSave() {
        createdAt = Instant.now();
    }
}
