package com.project.billing.dto;

import com.project.billing.entity.Payment;

import java.time.Instant;

public record PaymentDto(Long id, String planCode, long amountKopecks, String provider, String status, Instant createdAt) {
    public static PaymentDto from(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getPlanCode(), payment.getAmountKopecks(),
                payment.getProvider(), payment.getStatus(), payment.getCreatedAt());
    }
}
