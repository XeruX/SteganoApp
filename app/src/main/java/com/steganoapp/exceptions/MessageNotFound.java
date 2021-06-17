package com.steganoapp.exceptions;

public class MessageNotFound extends RuntimeException {
    public MessageNotFound(String message) {
        super(message);
    }
}
