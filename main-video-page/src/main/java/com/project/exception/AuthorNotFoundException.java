package com.project.exception;

/**
 * В userProfile нет пользователя с таким id → 400.
 */
public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(String message) {
        super(message);
    }
}
