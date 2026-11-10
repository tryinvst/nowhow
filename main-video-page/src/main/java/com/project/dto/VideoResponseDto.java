package com.project.dto;

import java.time.LocalDateTime;

/**
 * Карточка видео; moderationComment виден только автору и админу.
 */
public record VideoResponseDto(Long id, String name, String description, CategoryDto category,
                               Double durationSeconds, String coverUrl, String status, String moderationComment,
                               LocalDateTime createdAt, AuthorDto author, Long courseId, Integer lessonPosition,
                               boolean freePreview, Long size) { }
