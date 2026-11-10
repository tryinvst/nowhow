package com.project.testApp.dto.request;

import com.project.testApp.validation.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Данные для регистрации и редактирования пользователя.
 *
 * @param username    логин, уникальный
 * @param password    пароль: от 8 символов, заглавная буква и спецсимвол
 * @param phonenumber телефон
 * @param email       email, уникальный
 * @param role        тип аккаунта {@code master} или {@code student}: обязателен при регистрации,
 *                    при редактировании игнорируется
 */
public record PersonRequestDto(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be greater 8")
        String password,
        @NotBlank(message = "Phonenumber is required")
        String phonenumber,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,
        @ValidRole(nullable = true)
        String role) { }
