package com.project.testApp.dto.response;

/**
 * Минимальный ответ для других сервисов: без пароля, телефона и email.
 */
public record UserShortResponseDto(Long id, String username) {
}
