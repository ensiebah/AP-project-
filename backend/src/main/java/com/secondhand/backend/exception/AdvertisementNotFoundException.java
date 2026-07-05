package com.secondhand.backend.exception;

public class AdvertisementNotFoundException extends RuntimeException {

    public AdvertisementNotFoundException(String message) {
        super(message);
    }
}