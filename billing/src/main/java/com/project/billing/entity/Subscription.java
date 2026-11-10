package com.project.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Одна строка на пользователя: подписка активна, пока expiresAt в будущем.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    private Long userId;
    @Column(nullable = false)
    private String planCode;
    @Column(nullable = false)
    private Instant startedAt;
    @Column(nullable = false)
    private Instant expiresAt;

    public Subscription() {
    }

    public Subscription(Long userId) {
        this.userId = userId;
    }

    public boolean isActive() {
        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    public Long getUserId() {
        return userId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
