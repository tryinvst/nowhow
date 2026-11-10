package com.project.billing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlanUpdateRequestDto(@NotBlank(message = "Укажите title") String title,
                                   @NotNull @Positive(message = "Цена должна быть больше нуля") Long priceKopecks,
                                   @NotNull(message = "Укажите active") Boolean active) { }
