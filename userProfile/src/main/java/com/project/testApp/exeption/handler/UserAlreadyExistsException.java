package com.project.testApp.exeption.handler;

/**
 * {@code username} или email уже заняты → 409.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
