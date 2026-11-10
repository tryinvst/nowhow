package com.project.util;

import com.project.config.PaginationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Создаёт {@link Pageable} из параметров запроса и проверяет их границы.
 */
@Component
public class PageRequestFactory {

    private final PaginationProperties properties;

    public PageRequestFactory(PaginationProperties properties) {
        this.properties = properties;
    }

    /**
     * @param page номер страницы с нуля
     * @param size размер страницы, от 1 до {@link PaginationProperties#maxPageSize()}
     * @param sort порядок сортировки
     * @return страница для запроса в репозиторий
     * @throws IllegalArgumentException если page или size вне допустимых границ
     */
    public Pageable create(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > properties.maxPageSize()) {
            throw new IllegalArgumentException("page от 0, size от 1 до " + properties.maxPageSize());
        }
        return PageRequest.of(page, size, sort);
    }
}
