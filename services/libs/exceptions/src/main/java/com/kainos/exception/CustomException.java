package com.kainos.exception;

public abstract class CustomException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "NO MESSAGE IN EXCEPTION";

    public CustomException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
    }
}

