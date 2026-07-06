package com.secondhand.backend.exception;

public class BlockedUserException extends RuntimeException{
    public BlockedUserException(String message){
        super(message);
    }
}
