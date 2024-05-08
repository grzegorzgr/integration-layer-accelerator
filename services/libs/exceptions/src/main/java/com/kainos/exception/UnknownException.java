package com.kainos.exception;

import lombok.Builder;

public class UnknownException extends CustomException {

    @Builder
    public UnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
