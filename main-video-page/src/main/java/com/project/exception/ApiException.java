package com.project.exception;

import org.springframework.http.HttpStatus;

/**
 * Ошибка с явным HTTP-статусом: 401 (войдите), 402 (нужна подписка), 409 (конфликт) и т.п.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
