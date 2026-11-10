package com.project.testApp.exeption.handler;

/**
 * Пользователь не найден → 404.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
