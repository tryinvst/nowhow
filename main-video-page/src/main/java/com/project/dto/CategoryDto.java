package com.project.dto;

import com.project.common.Category;

public record CategoryDto(String code, String title) {
    public static CategoryDto from(Category category) {
        return category == null ? null : new CategoryDto(category.name(), category.getTitle());
    }
}
