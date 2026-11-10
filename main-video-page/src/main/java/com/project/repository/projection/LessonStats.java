package com.project.repository.projection;

/**
 * Сводка по урокам одного курса для карточки в каталоге.
 *
 * @param courseId             id курса
 * @param lessonsCount         число уроков
 * @param totalDurationSeconds суммарная длительность уроков в секундах
 */
public record LessonStats(Long courseId, Long lessonsCount, Double totalDurationSeconds) {
}
