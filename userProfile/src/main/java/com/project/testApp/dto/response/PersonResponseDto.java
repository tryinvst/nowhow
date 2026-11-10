package com.project.testApp.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Пароль (даже хэш) в ответах не возвращается.
 */
public record PersonResponseDto(@Schema(description = "ID пользователя", example = "55")
 Long id,String phonenumber,String username,String email,
 @Schema(description = "Роль", example = "Master") String role,
 @Schema(description = "Публичное имя", example = "Иван Петров") String displayName,
 @Schema(description = "Код специализации", example = "TILING") String specialization,
 String about, Integer experienceYears, String avatarUrl) {
}
