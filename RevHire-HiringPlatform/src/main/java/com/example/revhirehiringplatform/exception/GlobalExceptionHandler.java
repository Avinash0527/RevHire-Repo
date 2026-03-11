package com.example.revhirehiringplatform.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = "Validation failed";
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null) {
            errorMessage = fieldError.getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(errorMessage);
    }
}
