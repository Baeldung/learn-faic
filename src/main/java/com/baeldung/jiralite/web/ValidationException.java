package com.baeldung.jiralite.web;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
