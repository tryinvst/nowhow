package com.project.exception;

import org.springframework.http.HttpStatus;

/**
 * Курс не найден или не виден текущему пользователю: ответ 404.
 */
public class CourseNotFoundException extends ApiException {

    /**
     * @param id id курса из запроса
     */
    public CourseNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Курс с id " + id + " не найден");
    }
}
