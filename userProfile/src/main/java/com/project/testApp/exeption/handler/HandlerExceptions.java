package com.project.testApp.exeption.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HandlerExceptions {
    @ExceptionHandler (RuntimeException.class)
    public ResponseEntity userallready (RuntimeException exception) {
        System.out.println("Такой пользователь уже существует!");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
    @ExceptionHandler
    public ResponseEntity lastputuser (ExceptionInInitializerError exception) {
        System.out.println("Невозможно отредактировать пользователя, такого username не существует");
        return ResponseEntity.status (HttpStatus.NOT_FOUND).body("Невозможно отредактировать пользователя,"
          + "такого username не существует");
    }
    @ExceptionHandler
    public ResponseEntity succesputuser (Exception exception) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(exception.getMessage());
    }
    @ExceptionHandler
    public ResponseEntity lastdeleteuser (SecurityException exception) {
        System.out.println("Аккаунт успешно удален");
        return ResponseEntity.status (HttpStatus.CREATED).body("Аккаунт успешно удален");
    }
}
