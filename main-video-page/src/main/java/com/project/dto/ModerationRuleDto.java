package com.project.dto;

import com.project.entity.RuleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerationRuleDto(Long id,
                                @NotNull(message = "Укажите category") RuleCategory category,
                                @NotBlank(message = "Укажите pattern") @Size(max = 1000) String pattern,
                                String comment) { }
