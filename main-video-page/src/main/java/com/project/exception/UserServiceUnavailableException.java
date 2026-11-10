package com.project.exception;

/**
 * {@code userProfile} не ответил, автора проверить нельзя → 503.
 */
public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
