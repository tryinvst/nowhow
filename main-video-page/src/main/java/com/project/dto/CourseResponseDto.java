package com.project.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@code lessons} заполнены только в GET /courses/{id}; в списках - null.
 */
public record CourseResponseDto(Long id, String title, String description, CategoryDto category, String status,
                                String moderationComment, AuthorDto author, long lessonsCount,
                                double totalDurationSeconds, String coverUrl, LocalDateTime createdAt,
                                LocalDateTime publishedAt, List<VideoResponseDto> lessons) { }
