package com.project.testApp.exeption.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestControllerAdvice
public class HandlerExceptions {
    /**
     * Ошибки всегда text/plain: в сообщениях бывает ввод пользователя, как HTML он не отрисуется.
     */
    private static final MediaType PLAIN_TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);
    private static final Logger log = LoggerFactory.getLogger(HandlerExceptions.class);

    @ExceptionHandler
    public ResponseEntity<String> userAlreadyExists (UserAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> userNotFound (UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> badRequest (IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> unreadableBody (HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(PLAIN_TEXT).body("Некорректный JSON в теле запроса");
    }

    /**
     * Значение из запроса не возвращаем: строковый ответ при Accept: text/html отрисуется как HTML (XSS)
     */
    @ExceptionHandler
    public ResponseEntity<String> typeMismatch (MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(PLAIN_TEXT)
                .body("Некорректное значение параметра " + exception.getName());
    }

    /**
     * Ошибки {@code @Valid}: собираем сообщения по всем полям.
     */
    @ExceptionHandler
    public ResponseEntity<String> validationFailed (MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(PLAIN_TEXT).body(message);
    }

    @ExceptionHandler
    public ResponseEntity<String> unauthorized (AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> forbidden (AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(PLAIN_TEXT).body(exception.getMessage());
    }

    /**
     * Всё непредвиденное → 500, детали только в лог.
     * <p>Ошибки самого Spring MVC (нет параметра, кривой JSON, 405...) сохраняют свой статус.
     */
    @ExceptionHandler
    public ResponseEntity<String> unexpected (Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).contentType(PLAIN_TEXT).body(exception.getMessage());
        }
        log.error("Непредвиденная ошибка", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(PLAIN_TEXT).body("Внутренняя ошибка сервера");
    }
}
