package com.project.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Стабильный JSON для пагинации вместо сериализации Spring Page.
 */
public record PageResponseDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponseDto<T> of(Page<?> page, List<T> content) {
        return new PageResponseDto<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
