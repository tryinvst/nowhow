package com.project.billing.dto;

import com.project.billing.entity.Plan;

public record PlanDto(String code, String title, long priceKopecks, int periodDays, boolean active) {
    public static PlanDto from(Plan plan) {
        return new PlanDto(plan.getCode(), plan.getTitle(), plan.getPriceKopecks(), plan.getPeriodDays(), plan.isActive());
    }
}
