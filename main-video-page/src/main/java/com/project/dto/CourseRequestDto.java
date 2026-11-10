package com.project.dto;

import com.project.common.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @NotBlank(message = "Укажите название курса")
        @Size(max = 200, message = "Название - не длиннее 200 символов")
        String title,
        @Size(max = 5000, message = "Описание - не длиннее 5000 символов")
        String description,
        @NotNull(message = "Укажите категорию")
        Category category) { }
