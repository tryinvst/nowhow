package com.project.billing.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestControllerAdvice
public class BillingExceptionHandler {
    /**
     * Ошибки всегда text/plain: в сообщениях бывает ввод пользователя, как HTML он не отрисуется.
     */
    private static final MediaType PLAIN_TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private static final Logger log = LoggerFactory.getLogger(BillingExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<String> apiException(ApiException exception) {
        return ResponseEntity.status(exception.getStatus()).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> validationFailed(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(PLAIN_TEXT).body(message);
    }

    @ExceptionHandler
    public ResponseEntity<String> unreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(PLAIN_TEXT).body("Некорректный JSON в теле запроса");
    }

    @ExceptionHandler
    public ResponseEntity<String> forbidden(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    /**
     * Всё непредвиденное → 500, детали только в лог.
     */
    @ExceptionHandler
    public ResponseEntity<String> unexpected(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).contentType(PLAIN_TEXT).body(exception.getMessage());
        }
        log.error("Непредвиденная ошибка", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(PLAIN_TEXT).body("Внутренняя ошибка сервера");
    }
}
