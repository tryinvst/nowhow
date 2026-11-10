package com.project.billing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Тариф подписки на всю платформу; цена в копейках, чтобы не было ошибок округления.
 */
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    private String code;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private long priceKopecks;
    @Column(nullable = false)
    private int periodDays;
    private boolean active;

    public Plan() {
    }

    public Plan(String code, String title, long priceKopecks, int periodDays) {
        this.code = code;
        this.title = title;
        this.priceKopecks = priceKopecks;
        this.periodDays = periodDays;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public long getPriceKopecks() {
        return priceKopecks;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPriceKopecks(long priceKopecks) {
        this.priceKopecks = priceKopecks;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
