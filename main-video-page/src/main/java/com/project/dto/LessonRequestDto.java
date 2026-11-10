package com.project.dto;

import jakarta.validation.constraints.NotNull;

/**
 * {@code freePreview}: null - бесплатен только первый урок.
 */
public record LessonRequestDto(@NotNull(message = "Укажите videoId") Long videoId, Boolean freePreview) { }
