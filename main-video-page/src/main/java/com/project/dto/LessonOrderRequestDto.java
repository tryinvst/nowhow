package com.project.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Новый порядок уроков: все videoId курса, каждый ровно один раз.
 */
public record LessonOrderRequestDto(@NotEmpty(message = "Передайте videoIds") List<Long> videoIds) { }
