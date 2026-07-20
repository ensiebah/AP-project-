package com.secondhand.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🟢 Responsibility: Captures InvalidLoginExceptions and bypasses security filters
     * by returning HTTP 200 OK with an explicit ERROR format payload.
     */
    @ExceptionHandler(InvalidLoginException.class)
    public ResponseEntity<String> handleInvalidLogin(InvalidLoginException ex) {
        String formatPayload = "ERROR|" + ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.OK) // Change to OK so Spring Security doesn't intercept it
                .body(formatPayload);
    }
    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<String> handleDuplicateUsername(DuplicateUsernameException ex) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("ERROR|" + ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(DuplicateEmailException ex) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("ERROR|" + ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {

        String error = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body("ERROR|" + error);
    }
}