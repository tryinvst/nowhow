package com.project.entity;

public enum CourseStatus {
    /**
     * Виден только автору.
     */
    DRAFT,
    /**
     * Текст курса не прошёл автопроверку - ждёт админа.
     */
    ON_REVIEW,
    PUBLISHED,
    REJECTED
}
