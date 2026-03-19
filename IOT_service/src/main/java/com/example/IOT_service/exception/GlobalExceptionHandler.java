package com.example.IOT_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        HttpStatus status = "Invalid or inactive device".equals(ex.getMessage())
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
                "error", ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal server error: " + ex.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
