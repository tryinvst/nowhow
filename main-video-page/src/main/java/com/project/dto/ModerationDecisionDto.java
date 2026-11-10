package com.project.dto;

import jakarta.validation.constraints.Size;

/**
 * {@code reason} обязателен при отклонении - его увидит мастер.
 */
public record ModerationDecisionDto(@Size(max = 1000, message = "Причина - не длиннее 1000 символов") String reason) { }
