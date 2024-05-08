package com.kainos.exception;

import lombok.Builder;

public class TechnicalException extends CustomException {

    @Builder
    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
