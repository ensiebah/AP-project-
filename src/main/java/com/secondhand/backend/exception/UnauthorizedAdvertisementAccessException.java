package com.secondhand.backend.exception;

public class UnauthorizedAdvertisementAccessException extends RuntimeException {

    public UnauthorizedAdvertisementAccessException(String message) {
        super(message);
    }
}