package com.secondhand.backend.exception;

public class AlreadyRatedException extends RuntimeException {

    public AlreadyRatedException(String message) {
        super(message);
    }
}