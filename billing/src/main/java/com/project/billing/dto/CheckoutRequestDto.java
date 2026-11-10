package com.project.billing.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequestDto(@NotBlank(message = "Укажите planCode") String planCode) { }
