package com.project.billing.dto;

import java.time.Instant;

/**
 * {@code active=false} и null-поля - подписки нет или она закончилась.
 */
public record SubscriptionStatusDto(boolean active, String planCode, Instant startedAt, Instant expiresAt) { }
