package com.project.testApp.dto.request;

import com.project.common.Category;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Форма публичного профиля.
 *
 * @param displayName     имя, которое увидят ученики
 * @param specialization  для мастера обязательна, для ученика игнорируется
 * @param about           текст «О себе»
 * @param experienceYears стаж в годах
 */
public record ProfileUpdateRequestDto(
        @NotBlank(message = "Укажите имя, которое увидят ученики")
        @Size(max = 80, message = "Имя - не длиннее 80 символов")
        String displayName,
        Category specialization,
        @Size(max = 2000, message = "«О себе» - не длиннее 2000 символов")
        String about,
        @Min(value = 0, message = "Стаж не может быть отрицательным")
        @Max(value = 80, message = "Стаж - не больше 80 лет")
        Integer experienceYears) { }
